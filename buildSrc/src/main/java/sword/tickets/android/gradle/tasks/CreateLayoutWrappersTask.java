package sword.tickets.android.gradle.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.impldep.org.eclipse.jgit.annotations.NonNull;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public abstract class CreateLayoutWrappersTask extends DefaultTask {

    private final HashMap<String, String> _implicitTagNames = new HashMap<>();

    @Input
    public abstract ListProperty<File> getBootClassPath();

    @InputDirectory
    public abstract DirectoryProperty getInterfacesClasspath();

    @InputDirectory
    public abstract DirectoryProperty getResourcesDir();

    @Optional
    @InputDirectory
    public abstract DirectoryProperty getMainResourcesDir();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    private static String fromSnakeToPascalCase(String name) {
        final StringBuilder sb = new StringBuilder();
        final int length = name.length();
        boolean underscorePresent = false;
        for (int i = 0; i < length; i++) {
            final char ch = name.charAt(i);
            if (ch == '_') {
                underscorePresent = true;
            }
            else if ((underscorePresent || i == 0) && ch >= 'a' && ch <= 'z') {
                sb.append((char) (ch - 0x20));
                underscorePresent = false;
            }
            else {
                sb.append(ch);
                underscorePresent = false;
            }
        }

        return sb.toString();
    }

    private static URL defineClassDirUrl(String directoryPath) {
        try {
            return new URL("file://" + directoryPath + "/");
        }
        catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static URL defineJarUrl(String jarFilePath) {
        try {
            return new URL("jar", "", "file:" + jarFilePath + "!/");
        }
        catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static int findRequiredTypedParamsCount(String text) {
        int result = 0;
        final int length = text.length();
        boolean readingPlaceholder = false;
        boolean dollarFound = false;
        boolean lastWasBackslash = false;
        Integer number = null;
        for (int i = 0; i < length; i++) {
            final char ch = text.charAt(i);
            if (lastWasBackslash) {
                if (ch != 'n' && ch != '\'' && ch != '"' && ch != '@') {
                    throw new RuntimeException("Found unexpected escaped character '\\" + ch + "' in text " + text);
                }
                lastWasBackslash = false;
            }
            else if (readingPlaceholder) {
                if (ch == 's') {
                    result++;
                    readingPlaceholder = false;
                    dollarFound = false;
                }
                else if (ch == 'd') {
                    result++;
                    readingPlaceholder = false;
                    dollarFound = false;
                }
                else if (ch >= '0' && ch <= '9') {
                    if (dollarFound) {
                        throw new UnsupportedOperationException("Unable to include numbers after dollar in text " + text);
                    }

                    final int cypher = ch - '0';
                    number = (number != null)? number * 10 + cypher : cypher;
                }
                else if (ch == '$') {
                    if (dollarFound) {
                        throw new RuntimeException("Multiple dollar symbol found in the same placeholder in text " + text);
                    }
                    dollarFound = true;
                }
                else {
                    throw new UnsupportedOperationException("Unexpected character '" + ch + "' in placeholder in text " + text);
                }
            }
            else if (ch == '%') {
                readingPlaceholder = true;
            }
            else if (ch == '\\') {
                lastWasBackslash = true;
            }
            else if (ch == '\'' || ch == '"') {
                throw new RuntimeException("Found unexpected character " + ch + " in text " + text + ". It needs to be escaped.");
            }
            else if (i == 0 && ch == '@' && !text.startsWith("@string/")) {
                throw new RuntimeException("Found unexpected character @ at the beginning of text " + text + ". @ is reserved to reference other resources like '@string/abc'. If @ is expected to be displayed on the screen and it is in the very first position of the string, then it needs to be escaped '\\@'.");
            }
        }

        return result;
    }

    private static final class StringParserHandler extends DefaultHandler {

        final String fileName;
        final Map<String, String> result;

        String rootTag;
        boolean validRootTag;
        String definingStringName;
        StringBuilder definingStringText;

        StringParserHandler(String fileName, Map<String, String> result) {
            this.fileName = fileName;
            this.result = result;
        }

        @Override
        public void startElement(String uri, String lName, String qName, Attributes attr) {
            if (rootTag == null) {
                rootTag = qName;
                if ("resources".equals(qName)) {
                    validRootTag = true;
                }
            }
            else if (validRootTag && "string".equals(qName)) {
                if (definingStringName != null) {
                    throw new RuntimeException("'string' tag defined inside another string tag at " + fileName);
                }

                final int attrCount = (attr != null) ? attr.getLength() : 0;
                for (int attrIndex = 0; attrIndex < attrCount; attrIndex++) {
                    if ("name".equals(attr.getQName(attrIndex))) {
                        definingStringName = attr.getValue(attrIndex);
                        break;
                    }
                }

                if (definingStringName == null) {
                    throw new RuntimeException("Found 'string' resource without 'name' attribute at " + fileName);
                }
            }
            else if (validRootTag && definingStringName != null) {
                // According to the Android string resource documentation, there are other HTML tags allowed here.
                // TODO: Include all allowed tags here, when required
                if (!"i".equals(qName) && !"b".equals(qName) && !"u".equals(qName)) {
                    throw new RuntimeException("Found tag '" + qName + "' inside 'string' tag with name '" + definingStringName + "' at " + fileName);
                }
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            if (validRootTag && definingStringName != null) {
                if (definingStringText == null) {
                    definingStringText = new StringBuilder();
                }

                for (int i = 0; i < length; i++) {
                    definingStringText.append(ch[start + i]);
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if (validRootTag && "string".equals(qName)) {
                if (definingStringName == null) {
                    throw new RuntimeException("Found closing tag for 'string' without an starting matching one at " + fileName);
                }

                final String name = definingStringName;
                final String text = (definingStringText == null)? "" : definingStringText.toString();
                definingStringName = null;
                definingStringText = null;

                if (result.put(name, text) != null) {
                    throw new RuntimeException("Duplicated string name '" + name + "' at " + fileName);
                }
            }
        }
    }

    private static Set<String> obtainKnownPlaceholderStrings(File resourceDir, SAXParserFactory saxParserFactory) throws Exception {
        final Map<String, String> defaultResults = new HashMap<>();
        if (resourceDir.isDirectory()) {
            for (String subDirName : resourceDir.list()) {
                if ("values".equals(subDirName)) {
                    final File defaultValuesDir = new File(resourceDir, subDirName);
                    if (defaultValuesDir.isDirectory()) {
                        for (String fileName : defaultValuesDir.list()) {
                            if (fileName.endsWith(".xml")) {
                                final File file = new File(defaultValuesDir, fileName);
                                try (InputStream inStream = new FileInputStream(file)) {
                                    final StringParserHandler handler = new StringParserHandler(fileName, defaultResults);
                                    final SAXParser parser = saxParserFactory.newSAXParser();
                                    parser.parse(inStream, handler);
                                }
                            }
                        }

                        break;
                    }
                }
            }
        }

        final Set<String> knownPlaceholderStrings = new HashSet<>();
        for (String name : defaultResults.keySet()) {
            if (findRequiredTypedParamsCount(defaultResults.get(name)) > 0) {
                knownPlaceholderStrings.add(name);
            }
        }

        return knownPlaceholderStrings;
    }

    public CreateLayoutWrappersTask() {
        _implicitTagNames.put("AutoCompleteTextView", "android.widget.AutoCompleteTextView");
        _implicitTagNames.put("Button", "android.widget.Button");
        _implicitTagNames.put("CheckBox", "android.widget.CheckBox");
        _implicitTagNames.put("DatePicker", "android.widget.DatePicker");
        _implicitTagNames.put("DigitalClock", "android.widget.DigitalClock");
        _implicitTagNames.put("EditText", "android.widget.EditText");
        _implicitTagNames.put("FrameLayout", "android.widget.FrameLayout");
        _implicitTagNames.put("GridView", "android.widget.GridView");
        _implicitTagNames.put("HorizontalScrollView", "android.widget.HorizontalScrollView");
        _implicitTagNames.put("ImageButton", "android.widget.ImageButton");
        _implicitTagNames.put("ImageView", "android.widget.ImageView");
        _implicitTagNames.put("LinearLayout", "android.widget.LinearLayout");
        _implicitTagNames.put("ListView", "android.widget.ListView");
        _implicitTagNames.put("ProgressBar", "android.widget.ProgressBar");
        _implicitTagNames.put("RadioButton", "android.widget.RadioButton");
        _implicitTagNames.put("RelativeLayout", "android.widget.RelativeLayout");
        _implicitTagNames.put("ScrollView", "android.widget.ScrollView");
        _implicitTagNames.put("SeekBar", "android.widget.SeekBar");
        _implicitTagNames.put("Spinner", "android.widget.Spinner");
        _implicitTagNames.put("SurfaceView", "android.view.SurfaceView");
        _implicitTagNames.put("TextClock", "android.widget.TextClock");
        _implicitTagNames.put("TextView", "android.widget.TextView");
        _implicitTagNames.put("TimePicker", "android.widget.TimePicker");
        _implicitTagNames.put("VideoView", "android.widget.VideoView");
        _implicitTagNames.put("View", "android.view.View");
        _implicitTagNames.put("WebView", "android.webkit.WebView");
    }

    private interface Type {
    }

    private record ViewType(String typeName) implements Type {
    }

    private record LayoutType(String layoutName) implements Type {
    }

    private static final class ParserHandler extends DefaultHandler {

        final String fileName;
        final Set<String> knownPlaceholderStrings;

        final HashMap<String, Type> foundIdsAndTypes = new HashMap<>();
        final HashMap<String, String> idsAndWrappers = new HashMap<>();
        final HashSet<String> conflictingIds = new HashSet<>();
        final HashSet<String> foundLayouts = new HashSet<>();
        final HashSet<String> foundMultipleTimesLayout = new HashSet<>();

        String rootTag;
        String rootId;
        LinkedList<String> idHierarchy = new LinkedList<>();

        ParserHandler(String fileName, Set<String> knownPlaceholderStrings) {
            this.fileName = fileName;
            this.knownPlaceholderStrings = knownPlaceholderStrings;
        }

        private void assertValidId(String id) {
            final int length = id.length();
            if (id.charAt(0) < 'a' || id.charAt(0) > 'z') {
                final String message = "View id must start with lower case, but '" + id + "' in " + fileName + " does not.";
                System.err.println(message);
                throw new RuntimeException(message);
            }

            for (int i = 1; i < length; i++) {
                final char ch = id.charAt(i);
                if ((ch < 'a' || ch > 'z') && (ch < 'A' || ch > 'Z') && (ch < '0' || ch > '9')) {
                    final String message = "View id only can contain characters from a-z, A-Z or 0-9. Id '" + id + "' in " + fileName + " does not follow the rule.";
                    System.err.println(message);
                    throw new RuntimeException(message);
                }
            }
        }

        private void register(String id, Type value) {
            assertValidId(id);
            if (!conflictingIds.contains(id)) {
                if (foundIdsAndTypes.containsKey(id)) {
                    conflictingIds.add(id);
                    foundIdsAndTypes.remove(id);
                    idsAndWrappers.remove(id);
                }
                else {
                    foundIdsAndTypes.put(id, value);
                    String wrappingId = null;
                    if (idHierarchy.size() >= 2) {
                        for (String wid : idHierarchy) {
                            if (wid != null) {
                                wrappingId = wid;
                                break;
                            }
                        }
                    }

                    if (wrappingId != null) {
                        idsAndWrappers.put(id, wrappingId);
                    }
                }
            }
        }

        @Override
        public void startElement(String uri, String lName, String qName, Attributes attr) {
            boolean isRootTag = false;
            if (rootTag == null) {
                rootTag = qName;
                isRootTag = true;
            }

            if ("fragment".equals(qName)) {
                // Let's ignore it for now
                idHierarchy.addFirst(null);
            }
            else if ("include".equals(qName)) {
                String id = null;
                String layout = null;
                final int attrCount = (attr != null) ? attr.getLength() : 0;
                for (int attrIndex = 0; attrIndex < attrCount && !(id != null && layout != null); attrIndex++) {
                    if ("android:id".equals(attr.getQName(attrIndex))) {
                        final String value = attr.getValue(attrIndex);
                        if (value.startsWith("@+id/")) {
                            id = value.substring(5);
                        }
                        else if (value.startsWith("@id/")) {
                            id = value.substring(4);
                        }
                    }
                    else if ("layout".equals((attr.getQName(attrIndex)))) {
                        final String value = attr.getValue(attrIndex);
                        if (value.startsWith("@layout/")) {
                            layout = value.substring(8);
                        }
                    }
                }

                if (layout != null) {
                    if (foundLayouts.contains(layout)) {
                        foundMultipleTimesLayout.add(layout);
                    }
                    else {
                        foundLayouts.add(layout);
                    }

                    if (id != null) {
                        register(id, new LayoutType(layout));
                    }
                }
                idHierarchy.addFirst(id);
            }
            else {
                final int attrCount = (attr != null) ? attr.getLength() : 0;
                String id = null;
                for (int attrIndex = 0; attrIndex < attrCount; attrIndex++) {
                    final String value = attr.getValue(attrIndex);
                    if (value != null && value.startsWith("@string/")) {
                        final String stringName = value.substring(8);
                        if (knownPlaceholderStrings.contains(stringName)) {
                            throw new RuntimeException("Invalid string reference " + value +" in " + fileName + ". String requires placeholders.");
                        }
                    }

                    if ("android:id".equals(attr.getQName(attrIndex))) {
                        if (value.startsWith("@+id/")) {
                            id = value.substring(5);
                            register(id, new ViewType(qName));

                            if (isRootTag) {
                                rootId = id;
                            }
                        }
                        else if (value.startsWith("@id/")) {
                            id = value.substring(4);
                            register(id, new ViewType(qName));

                            if (isRootTag) {
                                rootId = id;
                            }
                        }
                    }
                }
                idHierarchy.addFirst(id);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            idHierarchy.removeFirst();
        }
    }

    private String tagNameToType(String tagName) {
        final String value = _implicitTagNames.get(tagName);
        return (value == null)? tagName : value;
    }

    private Set<String> checkAllPossibleParents(String className) {
        final HashSet<String> result = new HashSet<>();
        final Set<String> knownParents = mCastable.get(className);
        if (knownParents != null) {
            for (String possibleParent : knownParents) {
                result.add(possibleParent);
                result.addAll(checkAllPossibleParents(possibleParent));
            }
        }

        return result;
    }

    private String findBestCommonParent(ClassLoader loader, Iterable<String> types) {
        final Iterator<String> it = types.iterator();
        final String first = it.next();

        if (!it.hasNext()) {
            return first;
        }

        Set<String> commonParents = checkAllPossibleParents(first);
        while (it.hasNext()) {
            final Set<String> previousParents = commonParents;
            commonParents = new HashSet<>();
            final Set<String> possibleParents = checkAllPossibleParents(it.next());
            for (String type : possibleParents) {
                if (previousParents.contains(type)) {
                    commonParents.add(type);
                }
            }
        }

        while (commonParents.size() > 1) {
            String targetToRemove = null;
            outerFor:
            for (String source : commonParents) {
                for (String target : commonParents) {
                    if (!source.equals(target)) {
                        if (canBeCasted(loader, source, target)) {
                            targetToRemove = target;
                            break outerFor;
                        }
                    }
                }
            }

            if (targetToRemove == null) {
                throw new AssertionError("Unable to find a unique castable type");
            }

            commonParents.remove(targetToRemove);
        }

        return commonParents.iterator().next();
    }

    private String resolve(@NonNull ClassLoader loader, @NonNull Map<String, ParserHandler> handlers, @NonNull Map<String, Map<String, ParserHandler>> variantHandlers, String handlerId, @NonNull Map<String, String> typesResult, @NonNull Map<String, String> wrapResult, @NonNull Set<String> optionalIds, String nowWrapping, @NonNull Set<String> conflictingIds, String newRootId) {
        final ParserHandler handler = handlers.get(handlerId);
        if (handler == null) {
            throw new AssertionError("Unable to resolve layout " + handlerId);
        }

        final HashSet<String> allIds = new HashSet<>(handler.foundIdsAndTypes.keySet());
        final Map<String, ParserHandler> variants = new HashMap<>();
        for (String variantName : variantHandlers.keySet()) {
            final Map<String, ParserHandler> valueMap = variantHandlers.get(variantName);
            for (String id : valueMap.keySet()) {
                if (handlerId.equals(id)) {
                    final ParserHandler varHandler = valueMap.get(id);
                    for (String viewId : allIds) {
                        if (!varHandler.foundIdsAndTypes.containsKey(viewId)) {
                            optionalIds.add(viewId);
                        }
                    }

                    variants.put(variantName, varHandler);
                    break;
                }
            }
        }

        final HashSet<String> includedLayouts = new HashSet<>();
        for (Map.Entry<String, Type> entry : handler.foundIdsAndTypes.entrySet()) {
            final String id = entry.getKey();

            for (String variantName : variantHandlers.keySet()) {
                final Map<String, ParserHandler> valueMap = variantHandlers.get(variantName);
                for (String variantHandlerId : valueMap.keySet()) {
                    if (handlerId.equals(variantHandlerId)) {
                        variants.put(variantName, valueMap.get(variantHandlerId));
                        break;
                    }
                }
            }

            if (entry.getValue() instanceof ViewType) {
                if (typesResult.containsKey(id)) {
                    typesResult.remove(id);
                    wrapResult.remove(id);
                    conflictingIds.add(id);
                }
                else if (!conflictingIds.contains(id)) {
                    final HashSet<String> typesWithSameId = new HashSet<>();
                    typesWithSameId.add(tagNameToType(((ViewType) entry.getValue()).typeName));
                    for (String variantName : variants.keySet()) {
                        final HashMap<String, Type> variantIdsAndTypes = variants.get(variantName).foundIdsAndTypes;
                        final Type t;
                        if (variantIdsAndTypes.containsKey(id) && (t = variantIdsAndTypes.get(id)) instanceof ViewType) {
                            typesWithSameId.add(tagNameToType(((ViewType) t).typeName));
                        }
                    }

                    if (newRootId == null || !Objects.equals(id, handler.rootId)) {
                        typesResult.put(id, findBestCommonParent(loader, typesWithSameId));
                    }

                    final String wrapping = handler.idsAndWrappers.getOrDefault(id, nowWrapping);
                    if (Objects.equals(wrapping, handler.rootId) && newRootId != null && !newRootId.equals(wrapping)) {
                        wrapResult.put(id, newRootId);
                    }
                    else if (wrapping != null) {
                        wrapResult.put(id, wrapping);
                    }
                }
            }
            else {
                final String layoutName = ((LayoutType) entry.getValue()).layoutName;
                includedLayouts.add(layoutName);
                final String includeType = resolve(loader, handlers, variantHandlers, layoutName, typesResult, wrapResult, optionalIds, id, conflictingIds, id);

                if (typesResult.containsKey(id)) {
                    typesResult.remove(id);
                    wrapResult.remove(id);
                    conflictingIds.add(id);
                }
                else if (!conflictingIds.contains(id)) {
                    typesResult.put(id, includeType);
                    final String wrapping = handler.idsAndWrappers.getOrDefault(id, nowWrapping);
                    if (Objects.equals(wrapping, handler.rootId) && newRootId != null && !newRootId.equals(wrapping)) {
                        wrapResult.put(id, newRootId);
                    }
                    else if (wrapping != null) {
                        wrapResult.put(id, wrapping);
                    }
                }
            }
        }

        for (String layoutName : handler.foundLayouts) {
            if (!includedLayouts.contains(layoutName)) {
                resolve(loader, handlers, variantHandlers, layoutName, typesResult, wrapResult, optionalIds, nowWrapping, conflictingIds, null);
            }
        }

        if (variants.isEmpty()) {
            return handler.rootTag;
        }
        else {
            final HashSet<String> types = new HashSet<>();
            types.add(tagNameToType(handler.rootTag));
            for (ParserHandler variantHandler : variants.values()) {
                types.add(tagNameToType(variantHandler.rootTag));
            }

            while (types.size() > 1) {
                String sourceToRemove = null;
                outerFor:
                for (String source : types) {
                    for (String target : types) {
                        if (!source.equals(target) && canBeCasted(loader, source, target)) {
                            sourceToRemove = source;
                            break outerFor;
                        }
                    }
                }

                if (sourceToRemove == null) {
                    return findBestCommonParent(loader, types);
                }
                else {
                    types.remove(sourceToRemove);
                }
            }

            return types.iterator().next();
        }
    }

    private void findInterfaceCandidates(File folder, HashSet<String> candidates, String packageName) {
        for (String fileName : folder.list()) {
            final File file = new File(folder, fileName);
            if (fileName.endsWith(".class") && !file.isDirectory()) {
                candidates.add(packageName + "." + fileName.substring(0, fileName.length() - 6));
            }
            else if (file.isDirectory()) {
                findInterfaceCandidates(file, candidates, (packageName == null)? fileName : packageName + "." + fileName);
            }
        }
    }

    private record InterfaceInfo(List<String> extendingInterfaces, Map<String, String> methodNameAndType) {
    }

    private boolean resolveInterface(String interfaceName, Map<String, InterfaceInfo> interfaceInfo, Map<String, String> result) {
        final InterfaceInfo info = interfaceInfo.get(interfaceName);
        for (String extending : info.extendingInterfaces) {
            if (!interfaceInfo.containsKey(extending) || !resolveInterface(extending, interfaceInfo, result)) {
                return false;
            }
        }
        result.putAll(info.methodNameAndType);

        return true;
    }

    private final HashMap<String, HashSet<String>> mCastable = new HashMap<>();

    private static final HashMap<String, String> KNOWN_CASTS = new HashMap<>();

    private boolean canBeCasted(ClassLoader loader, String source, String target) {
        if ("java.lang.Object".equals(target) || source.equals(target)) {
            return true;
        }
        else if ("java.lang.Object".equals(source)) {
            return false;
        }

        HashSet<String> castable = mCastable.get(source);
        if (castable == null) {
            castable = new HashSet<>();

            final String knownCast = KNOWN_CASTS.get(source);
            if (knownCast != null) {
                castable.add(knownCast);
                mCastable.put(source, castable);
            }
            else {
                try {
                    final Class<?> cls = loader.loadClass(source);
                    for (Class<?> i : cls.getInterfaces()) {
                        castable.add(i.getName());
                    }

                    final Class<?> superClass = cls.getSuperclass();
                    if (superClass != null) {
                        castable.add(superClass.getName());
                    }
                    mCastable.put(source, castable);
                }
                catch (ClassNotFoundException e) {
                    return false;
                }
            }
        }

        for (String newSource : castable) {
            if (canBeCasted(loader, newSource, target)) {
                return true;
            }
        }

        return false;
    }

    private ArrayList<String> extractLayoutVariants(File resourceDir) {
        final ArrayList<String> variants = new ArrayList<>();
        boolean defaultLayoutFolderFound = false;
        for (String fileName : resourceDir.list()) {
            if (fileName.startsWith("layout")) {
                final File dir = new File(resourceDir, fileName);
                if (dir.isDirectory()) {
                    if ("layout".equals(fileName)) {
                        defaultLayoutFolderFound = true;
                    }
                    else if (fileName.startsWith("layout-")) {
                        variants.add(fileName.substring(7));
                    }
                }
            }
        }

        if (!defaultLayoutFolderFound) {
            throw new RuntimeException("Unable to find subfolder 'layout' in " + resourceDir.toString());
        }

        return variants;
    }

    private HashMap<String, ArrayList<String>> extractLayoutVariantsFileNames(File resourceDir, List<String> variants) {
        final HashMap<String, ArrayList<String>> variantFileNames = new HashMap<>();
        for (String variantName : variants) {
            final File subfolder = new File(resourceDir, "layout-" + variantName);
            final ArrayList<String> fileNames = new ArrayList<>();
            for (String fileName : subfolder.list()) {
                if (fileName.endsWith(".xml")) {
                    fileNames.add(fileName);
                }
            }
            variantFileNames.put(variantName, fileNames);
        }

        return variantFileNames;
    }

    private Map<String, ParserHandler> parseLayouts(File layoutsDir, Set<String> knownPlaceholderStrings, SAXParserFactory saxParserFactory) throws IOException, ParserConfigurationException, SAXException {
        final Map<String, ParserHandler> results = new HashMap<>();
        for (String fileName : layoutsDir.list()) {
            if (fileName.endsWith(".xml")) {
                final File file = new File(layoutsDir, fileName);
                try (InputStream inStream = new FileInputStream(file)) {
                    final ParserHandler handler = new ParserHandler(fileName, knownPlaceholderStrings);
                    final SAXParser parser = saxParserFactory.newSAXParser();
                    parser.parse(inStream, handler);

                    if (!handler.conflictingIds.isEmpty()) {
                        throw new RuntimeException("Duplicated id " + handler.conflictingIds.stream().reduce("", (a, b) -> a + ", " + b) + " in " + file);
                    }

                    final int fileNameLength = fileName.length();
                    final String layoutName = fileName.substring(0, fileNameLength - 4);
                    results.put(layoutName, handler);
                }
            }
        }

        return results;
    }

    @NonNull
    private HashSet<String> findMatchingInterfaces(@NonNull Map<String, Map<String, String>> resolvedInterfaces, @NonNull Map<String, String> idsAndTypesToMatch, @NonNull InputDirClassLoader loader) {
        final HashSet<String> matchingInterfaces = new HashSet<>();
        for (String interfaceName : resolvedInterfaces.keySet()) {
            final Map<String, String> interfMap = resolvedInterfaces.get(interfaceName);
            boolean allMatching = true;
            for (String methodName : interfMap.keySet()) {
                if (!idsAndTypesToMatch.containsKey(methodName) || !canBeCasted(loader, tagNameToType(idsAndTypesToMatch.get(methodName)), interfMap.get(methodName))) {
                    allMatching = false;
                    break;
                }
            }

            if (allMatching) {
                matchingInterfaces.add(interfaceName);
            }
        }

        return matchingInterfaces;
    }

    private void createBasicLayoutWrapper(@NonNull String layoutName, @NonNull File packageFile, @NonNull Map<String, String> idsAndTypes, @NonNull List<String> optionalIds, @NonNull Map<String, Map<String, String>> resolvedInterfaces, @NonNull InputDirClassLoader loader, @NonNull String packageName, @NonNull String rootType, @NonNull Map<String, String> idsAndWrappers) throws FileNotFoundException {
        final String classSimpleName = fromSnakeToPascalCase(layoutName) + "Layout";
        final File outFile = new File(packageFile, classSimpleName + ".java");
        try (PrintWriter writer = new PrintWriter(new FileOutputStream(outFile), true)) {
            final HashMap<String, String> idsAndTypesToMatch = new HashMap<>(idsAndTypes);
            idsAndTypesToMatch.put("view", rootType);

            final HashSet<String> matchingInterfaces = findMatchingInterfaces(resolvedInterfaces, idsAndTypesToMatch, loader);

            writer.println("// This file is autogenerated. Please do not edit it.");
            writer.println("package " + packageName + ";");
            writer.println();
            writer.println("import sword.tickets.android.layout.Layout;");
            writer.println("import sword.tickets.android.R;");
            writer.println();
            writer.println("import android.content.Context;");
            writer.println("import android.view.ContextThemeWrapper;");
            writer.println("import android.view.LayoutInflater;");
            writer.println("import android.view.ViewGroup;");
            writer.println();
            writer.println("import androidx.annotation.NonNull;");
            if (!optionalIds.isEmpty()) {
                writer.println("import androidx.annotation.Nullable;");
            }
            writer.println("import androidx.annotation.StyleRes;");
            writer.println();
            writer.println("import static sword.tickets.android.PreconditionUtils.ensureNonNull;");
            writer.println();

            final String extensions;
            if (matchingInterfaces.isEmpty()) {
                extensions = "";
            }
            else {
                StringBuilder sb = null;
                for (String interfName : matchingInterfaces) {
                    if (sb == null) {
                        sb = new StringBuilder(" implements ");
                    }
                    else {
                        sb.append(", ");
                    }
                    sb.append(interfName);
                }
                extensions = sb.toString();
            }

            writer.println("public final class " + classSimpleName + extensions + " {");
            writer.println();

            writer.println("    @NonNull");
            writer.println("    private final " + rootType + " _root;");
            for (Map.Entry<String, String> entry : idsAndTypes.entrySet()) {
                writer.println("    private " + tagNameToType(entry.getValue()) + ' ' + entry.getKey() + ";");
            }

            if (!optionalIds.isEmpty()) {
                if (optionalIds.size() >= 31) {
                    throw new UnsupportedOperationException("Unexpected having 31 or more optionals in a single layout");
                }

                writer.println("    private int mOptionalsAlreadyChecked;");
            }

            writer.println();
            writer.println("    private " + classSimpleName + "(@NonNull " + rootType + " root) {");
            writer.println("        ensureNonNull(root);");
            writer.println("        _root = root;");
            writer.println("    }");

            writer.println();
            writer.println("    @NonNull");
            writer.println("    @Override");
            writer.println("    public " + rootType + " view() {");
            writer.println("        return _root;");
            writer.println("    }");

            for (Map.Entry<String, String> entry : idsAndTypes.entrySet()) {
                final int indexOfOptional = optionalIds.indexOf(entry.getKey());
                writer.println();
                if (indexOfOptional >= 0) {
                    writer.println("    @Nullable");
                    writer.println("    public " + tagNameToType(entry.getValue()) + ' ' + entry.getKey() + "() {");
                    writer.println("        if ((mOptionalsAlreadyChecked & 0x" + Integer.toHexString(1 << indexOfOptional) + ") == 0) {");
                    final String wrapping = idsAndWrappers.get(entry.getKey());
                    writer.println("            " + entry.getKey() + " = " + ((wrapping != null)? wrapping + "()" : "_root") + ".findViewById(R.id." + entry.getKey() + ");");
                    writer.println("            mOptionalsAlreadyChecked |= 0x" + Integer.toHexString(1 << indexOfOptional) + ";");
                }
                else {
                    writer.println("    @NonNull");
                    writer.println("    public " + tagNameToType(entry.getValue()) + ' ' + entry.getKey() + "() {");
                    writer.println("        if (" + entry.getKey() + " == null) {");
                    final String wrapping = idsAndWrappers.get(entry.getKey());
                    writer.println("            " + entry.getKey() + " = " + ((wrapping != null)? wrapping + "()" : "_root") + ".findViewById(R.id." + entry.getKey() + ");");
                }
                writer.println("        }");
                writer.println();
                writer.println("        return " + entry.getKey() + ";");
                writer.println("    }");
            }

            writer.println();
            writer.println("    @NonNull");
            writer.println("    public static " + classSimpleName + " attachWithLayoutInflater(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {");
            writer.println("        final int position = parent.getChildCount();");
            writer.println("        inflater.inflate(R.layout." + layoutName + ", parent, true);");
            writer.println("        return new " + classSimpleName + "((" + rootType + ") parent.getChildAt(position));");
            writer.println("    }");
            writer.println();
            writer.println("    @NonNull");
            writer.println("    public static " + classSimpleName + " attach(@NonNull ViewGroup parent) {");
            writer.println("        return attachWithLayoutInflater(LayoutInflater.from(parent.getContext()), parent);");
            writer.println("    }");
            writer.println();
            writer.println("    @NonNull");
            writer.println("    public static " + classSimpleName + " createWithLayoutInflater(@NonNull LayoutInflater inflater, ViewGroup parent) {");
            writer.println("        return new " + classSimpleName + "((" + rootType + ") inflater.inflate(R.layout." + layoutName + ", parent, false));");
            writer.println("    }");
            writer.println();
            writer.println("    @NonNull");
            writer.println("    public static " + classSimpleName + " create(@NonNull ViewGroup parent) {");
            writer.println("        return createWithLayoutInflater(LayoutInflater.from(parent.getContext()), parent);");
            writer.println("    }");
            writer.println();
            writer.println("    @NonNull");
            writer.println("    public static " + classSimpleName + " createWithTheme(@StyleRes int styleResId, @NonNull ViewGroup parent) {");
            writer.println("        final Context context = parent.getContext();");
            writer.println("        final Context themedContext = new ContextThemeWrapper(context, styleResId);");
            writer.println("        return createWithLayoutInflater(LayoutInflater.from(themedContext), parent);");
            writer.println("    }");
            writer.println("}");
        }
    }

    private void createLayoutForActivityWrapper(@NonNull String layoutName, @NonNull File packageFile, @NonNull Map<String, String> idsAndTypes, @NonNull List<String> optionalIds, @NonNull Map<String, Map<String, String>> resolvedInterfaces, @NonNull InputDirClassLoader loader, @NonNull String packageName, @NonNull String rootType, @NonNull Map<String, String> idsAndWrappers) throws FileNotFoundException {
        final String classSimpleName = fromSnakeToPascalCase(layoutName) + "LayoutForActivity";
        final File outFile = new File(packageFile, classSimpleName + ".java");
        try (PrintWriter writer = new PrintWriter(new FileOutputStream(outFile), true)) {
            final HashSet<String> matchingInterfaces = findMatchingInterfaces(resolvedInterfaces, idsAndTypes, loader);

            writer.println("// This file is autogenerated. Please do not edit it.");
            writer.println("package " + packageName + ";");
            writer.println();
            writer.println("import android.app.Activity;");
            writer.println("import android.view.View;");
            writer.println();
            writer.println("import sword.tickets.android.R;");
            writer.println();
            writer.println("import androidx.annotation.NonNull;");
            if (!optionalIds.isEmpty()) {
                writer.println("import androidx.annotation.Nullable;");
            }
            writer.println();
            writer.println("import static sword.tickets.android.PreconditionUtils.ensureNonNull;");
            writer.println();

            final String extensions;
            if (matchingInterfaces.isEmpty()) {
                extensions = "";
            }
            else {
                StringBuilder sb = null;
                for (String interfName : matchingInterfaces) {
                    if (sb == null) {
                        sb = new StringBuilder(" implements ");
                    }
                    else {
                        sb.append(", ");
                    }
                    sb.append(interfName);
                }
                extensions = sb.toString();
            }

            writer.println("public final class " + classSimpleName + extensions + " {");
            writer.println();

            writer.println("    @NonNull");
            writer.println("    private final View mDecorView;");
            for (Map.Entry<String, String> entry : idsAndTypes.entrySet()) {
                writer.println("    private " + tagNameToType(entry.getValue()) + ' ' + entry.getKey() + ";");
            }

            if (!optionalIds.isEmpty()) {
                if (optionalIds.size() >= 31) {
                    throw new UnsupportedOperationException("Unexpected having 31 or more optionals in a single layout");
                }

                writer.println("    private int mOptionalsAlreadyChecked;");
            }

            writer.println();
            writer.println("    private " + classSimpleName + "(@NonNull View decorView) {");
            writer.println("        ensureNonNull(decorView);");
            writer.println("        mDecorView = decorView;");
            writer.println("    }");

            for (Map.Entry<String, String> entry : idsAndTypes.entrySet()) {
                final int indexOfOptional = optionalIds.indexOf(entry.getKey());
                writer.println();
                if (indexOfOptional >= 0) {
                    writer.println("    @Nullable");
                    writer.println("    public " + tagNameToType(entry.getValue()) + ' ' + entry.getKey() + "() {");
                    writer.println("        if ((mOptionalsAlreadyChecked & 0x" + Integer.toHexString(1 << indexOfOptional) + ") == 0) {");
                    final String wrapping = idsAndWrappers.get(entry.getKey());
                    writer.println("            " + entry.getKey() + " = " + ((wrapping != null) ? wrapping + "()" : "mDecorView") + ".findViewById(R.id." + entry.getKey() + ");");
                    writer.println("            mOptionalsAlreadyChecked |= 0x" + Integer.toHexString(1 << indexOfOptional) + ";");
                }
                else {
                    writer.println("    @NonNull");
                    writer.println("    public " + tagNameToType(entry.getValue()) + ' ' + entry.getKey() + "() {");
                    writer.println("        if (" + entry.getKey() + " == null) {");
                    final String wrapping = idsAndWrappers.get(entry.getKey());
                    writer.println("            " + entry.getKey() + " = " + ((wrapping != null) ? wrapping + "()" : "mDecorView") + ".findViewById(R.id." + entry.getKey() + ");");
                }
                writer.println("        }");
                writer.println();
                writer.println("        return " + entry.getKey() + ";");
                writer.println("    }");
            }

            writer.println();
            writer.println("    @NonNull");
            writer.println("    public static " + classSimpleName + " attach(@NonNull Activity activity) {");
            writer.println("        activity.setContentView(R.layout." + layoutName + ");");
            writer.println("        return new " + classSimpleName + "(activity.getWindow().getDecorView());");
            writer.println("    }");
            writer.println("}");
        }
    }

    @TaskAction
    public void createLayoutWrappers() {
        final File interfacesClasspath = getInterfacesClasspath().get().getAsFile();
        final HashSet<String> interfaceCandidates = new HashSet<>();
        findInterfaceCandidates(interfacesClasspath, interfaceCandidates, null);

        final ArrayList<URL> urlList = new ArrayList<>();
        for (File file : getBootClassPath().get()) {
            urlList.add(file.toString().endsWith(".jar")? defineJarUrl(file.toString()) : defineClassDirUrl(file.toString()));
        }

        final URL[] urls = urlList.toArray(new URL[0]);

        try {
            final InputDirClassLoader loader = new InputDirClassLoader(interfacesClasspath, new URLClassLoader(urls));
            final HashMap<String, InterfaceInfo> interfaceInfo = new HashMap<>();
            for (String candidate : interfaceCandidates) {
                final Class<?> cls = loader.loadClass(candidate);
                if (cls.isInterface()) {
                    final ArrayList<String> extendingInterfaces = new ArrayList<>();
                    for (Class<?> extending : cls.getInterfaces()) {
                        extendingInterfaces.add(extending.getName());
                    }

                    final HashMap<String, String> methodNameAndType = new HashMap<>();
                    boolean allGetters = true;
                    for (Method method : cls.getDeclaredMethods()) {
                        if (!method.isDefault()) {
                            if (method.getParameterCount() != 0) {
                                allGetters = false;
                            }
                            else {
                                methodNameAndType.put(method.getName(), method.getReturnType().getName());
                            }
                        }
                    }

                    if (allGetters) {
                        interfaceInfo.put(candidate, new InterfaceInfo(extendingInterfaces, methodNameAndType));
                    }
                }
            }

            final HashMap<String, Map<String, String>> resolvedInterfaces = new HashMap<>();
            for (String interfaceName : interfaceInfo.keySet()) {
                final HashMap<String, String> methodNameAndType = new HashMap<>();
                if (resolveInterface(interfaceName, interfaceInfo, methodNameAndType)) {
                    resolvedInterfaces.put(interfaceName, methodNameAndType);
                }
            }

            final File resourceDir = getResourcesDir().get().getAsFile();
            final ArrayList<String> variants = extractLayoutVariants(resourceDir);

            final File mainResourceDir = getMainResourcesDir().isPresent()? getMainResourcesDir().get().getAsFile() : null;
            final ArrayList<String> mainVariants = (mainResourceDir != null)? extractLayoutVariants(resourceDir) : new ArrayList<>();

            final File defaultLayoutsDir = new File(resourceDir, "layout");
            final File mainDefaultLayoutsDir = new File(mainResourceDir, "layout");

            final String packageName = "sword.tickets.android.layout";
            final File outputDir = getOutputDir().get().getAsFile();
            File currentFile = outputDir;
            for (String split : packageName.split("\\.")) {
                currentFile = new File(currentFile, split);
            }

            final File packageFile = currentFile;
            packageFile.mkdirs();

            final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            final Set<String> knownPlaceholderStrings = obtainKnownPlaceholderStrings(resourceDir, saxParserFactory);

            final Map<String, ParserHandler> parseResults = parseLayouts(defaultLayoutsDir, knownPlaceholderStrings, saxParserFactory);
            final Map<String, Map<String, ParserHandler>> variantParseResults = new HashMap<>();
            for (String variantName : variants) {
                final File layoutsDir = new File(resourceDir, "layout-" + variantName);
                if (layoutsDir.isDirectory()) {
                    variantParseResults.put(variantName, parseLayouts(layoutsDir, knownPlaceholderStrings, saxParserFactory));
                }
            }

            final Map<String, ParserHandler> allParseResults = new HashMap<>();
            allParseResults.putAll(parseResults);

            final Map<String, Map<String, ParserHandler>> allVariantParseResults = new HashMap<>();
            allVariantParseResults.putAll(variantParseResults);

            if (mainResourceDir != null) {
                final Set<String> mainKnownPlaceholderStrings = obtainKnownPlaceholderStrings(mainResourceDir, saxParserFactory);
                final Map<String, ParserHandler> mainParseResults = parseLayouts(mainDefaultLayoutsDir, mainKnownPlaceholderStrings, saxParserFactory);
                final Map<String, Map<String, ParserHandler>> mainVariantParseResults = new HashMap<>();
                for (String variantName : mainVariants) {
                    final File layoutsDir = new File(mainResourceDir, "layout-" + variantName);
                    if (layoutsDir.isDirectory()) {
                        final Map<String, ParserHandler> result = parseLayouts(layoutsDir, knownPlaceholderStrings, saxParserFactory);
                        mainVariantParseResults.put(variantName, result);

                        final Map<String, ParserHandler> results = allVariantParseResults.computeIfAbsent(variantName, k -> new HashMap<>());
                        results.putAll(result);
                    }
                }

                allParseResults.putAll(mainParseResults);
            }

            for (Map.Entry<String, ParserHandler> resultEntry : parseResults.entrySet()) {
                final HashMap<String, String> idsAndTypes = new HashMap<>();
                final HashMap<String, String> idsAndWrappers = new HashMap<>();
                final HashSet<String> conflictingIds = new HashSet<>();
                final HashSet<String> optionalIds = new HashSet<>();
                final String rootTag = resolve(loader, allParseResults, allVariantParseResults, resultEntry.getKey(), idsAndTypes, idsAndWrappers, optionalIds, null, conflictingIds, null);
                final String rootType = tagNameToType(rootTag);
                final String layoutName = resultEntry.getKey();
                createBasicLayoutWrapper(layoutName, packageFile, idsAndTypes, new ArrayList<>(optionalIds), resolvedInterfaces, loader, packageName, rootType, idsAndWrappers);
                createLayoutForActivityWrapper(layoutName, packageFile, idsAndTypes, new ArrayList<>(optionalIds), resolvedInterfaces, loader, packageName, rootType, idsAndWrappers);
            }
        }
        catch (Throwable t) {
            throw new UnsupportedOperationException("Failure on creating layout wrappers", t);
        }
    }
}