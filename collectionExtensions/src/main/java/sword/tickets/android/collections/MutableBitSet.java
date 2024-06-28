package sword.tickets.android.collections;

import java.util.Arrays;

import sword.collections.AbstractIntTransformer;
import sword.collections.EmptyCollectionException;
import sword.collections.ImmutableIntKeyMap;
import sword.collections.ImmutableIntPairMap;
import sword.collections.ImmutableIntSet;
import sword.collections.IntFunction;
import sword.collections.IntKeyMap;
import sword.collections.IntPairMap;
import sword.collections.IntSet;
import sword.collections.IntToIntFunction;
import sword.collections.IntTransformer;
import sword.collections.MutableIntArraySet;
import sword.collections.MutableIntSet;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;

/**
 * Efficient implementation for a mutable Set of natural values.
 * 'Set' must be understood as a collection where its elements cannot be repeated.
 * 'natural' are composed by zero and positive values, but not negative.
 * Then, this set does not admit negative values.
 * <p>
 * This class also implements the {@link Iterable} interface, which
 * ensures that the for-each construction can be used.
 * <p>
 * Note that this class does not implement {@link MutableIntSet}. This is because
 * {@link MutableIntSet#add(int)} admits also negative numbers, which is not possible here.
 * Then MutableBitSet should not be able to cast to MutableIntSet because then the add would
 * not satisfy the contract.
 */
public final class MutableBitSet implements IntSet {

    private static final int OFFSET_BITS_IN_INDEX = 5; // int has 32 bits in Java. 1 << 5 == 32
    private static final int OFFSET_MASK = (1 << OFFSET_BITS_IN_INDEX) - 1;

    private int[] mValue;
    private transient int mSize = -1;

    @NonNull
    public static MutableBitSet empty() {
        return new MutableBitSet(null);
    }

    private MutableBitSet(int[] value) {
        mValue = value;
    }

    @Override
    public boolean contains(int value) {
        if (mValue == null || value < 0) {
            return false;
        }

        final int wordIndex = value >>> OFFSET_BITS_IN_INDEX;
        if (wordIndex >= mValue.length) {
            return false;
        }

        final int mask = 1 << (value & OFFSET_MASK);
        return (mValue[wordIndex] & mask) != 0;
    }

    @Override
    public int size() {
        if (mSize != -1) {
            return mSize;
        }

        if (mValue == null) {
            mSize = 0;
            return 0;
        }

        final int length = mValue.length;
        int sum = 0;
        for (int wordIndex = 0; wordIndex < length; wordIndex++) {
            final int wordValue = mValue[wordIndex];
            int mask = 1;
            while (mask != 0) {
                if ((wordValue & mask) != 0) {
                    ++sum;
                }

                mask <<= 1;
            }
        }

        mSize = sum;
        return sum;
    }

    @Override
    public boolean isEmpty() {
        return mValue == null;
    }

    /**
     * Include the given value within this set.
     * @param value Value to be included.
     * @return True if the value was not present and this operation modified
     *         this set, or false if it was already included.
     * @throws java.lang.IllegalArgumentException if value is negative.
     */
    public boolean add(int value) {
        if (value < 0) {
            throw new IllegalArgumentException();
        }

        final int wordIndex = value >> OFFSET_BITS_IN_INDEX;
        final int bitMask = 1 << (value & OFFSET_MASK);

        if (mValue == null) {
            mValue = new int[wordIndex + 1];
            mValue[wordIndex] |= bitMask;
            mSize = 1;
            return true;
        }
        else if (wordIndex >= mValue.length) {
            final int[] newValue = new int[wordIndex + 1];
            System.arraycopy(mValue, 0, newValue, 0, mValue.length);
            newValue[wordIndex] |= bitMask;
            mValue = newValue;

            if (mSize > 0) {
                mSize++;
            }
            return true;
        }
        else if ((mValue[wordIndex] & bitMask) == 0) {
            mValue[wordIndex] |= bitMask;
            if (mSize > 0) {
                mSize++;
            }
            return true;
        }

        return false;
    }

    /**
     * Removes the given value from the set if present, or includes it if not present.
     * @param value Value to be removed or added.
     * @throws java.lang.IllegalArgumentException if value is negative.
     */
    public void flip(int value) {
        if (value < 0) {
            throw new IllegalArgumentException();
        }

        final int wordIndex = value >> OFFSET_BITS_IN_INDEX;
        final int bitMask = 1 << (value & OFFSET_MASK);

        if (mValue == null) {
            mValue = new int[wordIndex + 1];
            mValue[wordIndex] |= bitMask;
            mSize = 1;
        }
        else if (wordIndex >= mValue.length) {
            final int[] newValue = new int[wordIndex + 1];
            System.arraycopy(mValue, 0, newValue, 0, mValue.length);
            newValue[wordIndex] |= bitMask;
            mValue = newValue;

            if (mSize > 0) {
                mSize++;
            }
        }
        else if ((mValue[wordIndex] & bitMask) != 0) {
            mValue[wordIndex] &= ~bitMask;
            if (mSize > 0) {
                --mSize;
            }

            int index = mValue.length - 1;
            while (index >= 0 && mValue[index] == 0) {
                index--;
            }

            if (index < 0) {
                mValue = null;
            }
        }
        else {
            mValue[wordIndex] |= bitMask;
            if (mSize > 0) {
                mSize++;
            }
        }
    }

    /**
     * Ensures that the value is contained in the set if present is true,
     * or not contained if present is false.
     * <p>
     * This method may or may not change the current state of this set.
     * This will return true in case the state of this collection is changed.
     *
     * @param value Value that has to be checked for presence.
     * @param present Whether we want to have the value included or not after calling this method.
     * @return Whether the collection state has changed after this operation or not.
     */
    public boolean syncPresence(int value, boolean present) {
        if (value < 0) {
            throw new IllegalArgumentException();
        }

        final int wordIndex = value >> OFFSET_BITS_IN_INDEX;
        final int bitMask = 1 << (value & OFFSET_MASK);

        if (mValue == null) {
            if (present) {
                mValue = new int[wordIndex + 1];
                mValue[wordIndex] |= bitMask;
                mSize = 1;
                return true;
            }
            else {
                return false;
            }
        }
        else if (wordIndex >= mValue.length) {
            if (present) {
                final int[] newValue = new int[wordIndex + 1];
                System.arraycopy(mValue, 0, newValue, 0, mValue.length);
                newValue[wordIndex] |= bitMask;
                mValue = newValue;

                if (mSize >= 0) {
                    mSize++;
                }
                return true;
            }
            else {
                return false;
            }
        }
        else {
            final boolean currentlyPresent = (mValue[wordIndex] & bitMask) != 0;
            if (present && !currentlyPresent) {
                mValue[wordIndex] |= bitMask;
                if (mSize > 0) {
                    mSize++;
                }
                return true;
            }
            else if (!present && currentlyPresent) {
                mValue[wordIndex] &= ~bitMask;
                if (mSize > 0) {
                    --mSize;
                }

                int index = mValue.length - 1;
                while (index >= 0 && mValue[index] == 0) {
                    index--;
                }

                if (index < 0) {
                    mValue = null;
                }
                else if (index < mValue.length - 1) {
                    final int[] newValue = new int[index + 1];
                    System.arraycopy(mValue, 0, newValue, 0, index + 1);
                    mValue = newValue;
                }

                return true;
            }
        }

        return false;
    }

    public boolean clear() {
        final boolean hasChanged = mValue != null;
        mValue = null;
        mSize = 0;
        return hasChanged;
    }

    @NonNull
    @CheckResult
    public MutableBitSet donate() {
        final MutableBitSet newSet = new MutableBitSet(mValue);
        newSet.mSize = mSize;
        mValue = null;
        mSize = 0;
        return newSet;
    }

    public void removeAt(int index) {
        remove(valueAt(index));
    }

    public boolean remove(int value) {
        if (value < 0 || mValue == null) {
            return false;
        }

        final int wordIndex = value >> OFFSET_BITS_IN_INDEX;
        final int bitMask = 1 << (value & OFFSET_MASK);

        if (mValue.length <= wordIndex || (mValue[wordIndex] & bitMask) == 0) {
            return false;
        }
        else {
            mValue[wordIndex] &= ~bitMask;
            if (mSize > 0) {
                --mSize;
            }

            int index = mValue.length - 1;
            while (index >= 0 && mValue[index] == 0) {
                index--;
            }

            if (index < 0) {
                mValue = null;
            }
            else if (index < mValue.length - 1) {
                final int[] newValue = new int[index + 1];
                System.arraycopy(mValue, 0, newValue, 0, index + 1);
                mValue = newValue;
            }

            return true;
        }
    }

    @Override
    public int min() throws EmptyCollectionException {
        if (mValue == null) {
            throw new EmptyCollectionException();
        }
        return valueAt(0);
    }

    @Override
    public int max() throws EmptyCollectionException {
        final int length = (mValue != null)? mValue.length : 0;
        for (int i = length - 1; i >= 0; i--) {
            int index = OFFSET_MASK;
            for (int word = mValue[i]; word != 0; word <<= 1) {
                if ((word & Integer.MIN_VALUE) != 0) {
                    return i * 32 + index;
                }
                --index;
            }
        }

        throw new EmptyCollectionException();
    }

    @Override
    public <V> IntKeyMap<V> assign(IntFunction<? extends V> function) {
        final ImmutableIntKeyMap.Builder<V> builder = new ImmutableIntKeyMap.Builder<>();
        for (int key : this) {
            builder.put(key, function.apply(key));
        }

        return builder.build();
    }

    @Override
    public IntPairMap assignToInt(IntToIntFunction function) {
        final ImmutableIntPairMap.Builder builder = new ImmutableIntPairMap.Builder();
        for (int key : this) {
            builder.put(key, function.apply(key));
        }

        return builder.build();
    }

    @NonNull
    @Override
    public ImmutableIntSet toImmutable() {
        return mutate().toImmutable();
    }

    @NonNull
    @Override
    public MutableIntSet mutate() {
        final MutableIntArraySet result = MutableIntArraySet.empty();
        for (int value : this) {
            result.add(value);
        }

        return result;
    }

    private final class Iterator extends AbstractIntTransformer {

        private int mWordIndex;
        private int mOffset;
        private int mNextValue;

        private Iterator() {
            findFirst();
        }

        private void findFirst() {
            boolean found = false;
            if (mValue != null) {
                while (!found && mWordIndex < mValue.length) {
                    final int value = mValue[mWordIndex];
                    while (!found && mOffset <= OFFSET_MASK) {
                        found = (value & (1 << mOffset)) != 0;

                        if (!found) {
                            ++mOffset;
                        }
                    }

                    if (mOffset > OFFSET_MASK) {
                        mOffset = 0;
                        ++mWordIndex;
                    }
                }
            }

            mNextValue = found? (mWordIndex << OFFSET_BITS_IN_INDEX) + mOffset : -1;
        }

        @Override
        public boolean hasNext() {
            return mNextValue != -1;
        }

        @Override
        public Integer next() {
            final int result = mNextValue;
            ++mOffset;
            findFirst();

            return result;
        }
    }

    @Override
    public IntTransformer iterator() {
        return new Iterator();
    }

    static class Builder implements IntSet.Builder {
        private MutableBitSet set = empty();

        @Override
        public Builder add(int value) {
            set.add(value);
            return this;
        }

        @Override
        public MutableBitSet build() {
            return set;
        }
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        else if (!(object instanceof MutableBitSet)) {
            return false;
        }

        final MutableBitSet that = (MutableBitSet) object;
        return Arrays.equals(mValue, that.mValue);
    }

    @Override
    public int hashCode() {
        return size();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append('(');
        boolean itemAdded = false;

        for (int value : this) {
            if (itemAdded) {
                sb.append(',');
            }

            sb.append(value);
            itemAdded = true;
        }

        return sb.append(')').toString();
    }
}
