//
// $Id: ArrayIntSet.java,v 1.1 2002/02/03 07:10:16 mdb Exp $
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001 Michael Bayne
// 
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.samskivert.util;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Provides an {@link IntSet} implementation using a sorted array of
 * integers to maintain the contents of the set.
 */
public class ArrayIntSet extends AbstractSet
    implements IntSet
{
    // documentation inherited from interface
    public int size ()
    {
        return _size;
    }

    // documentation inherited from interface
    public boolean isEmpty ()
    {
        return _size == 0;
    }

    // documentation inherited from interface
    public boolean contains (Object o)
    {
        return contains(((Integer)o).intValue());
    }

    // documentation inherited from interface
    public boolean contains (int value)
    {
        return (binarySearch(value) >= 0);
    }

    // documentation inherited from interface
    public Iterator iterator ()
    {
        return new Iterator() {
            public boolean hasNext () {
                return (_pos < _size);
            }

            public Object next () {
                if (_pos == _size) {
                    throw new NoSuchElementException();
                } else {
                    return new Integer(_values[_pos++]);
                }
            }

            public void remove () {
                throw new UnsupportedOperationException();
            }

            protected int _pos;
        };
    }

    // documentation inherited from interface
    public Object[] toArray ()
    {
        return toArray(new Integer[_size]);
    }

    // documentation inherited from interface
    public Object[] toArray (Object[] a)
    {
        for (int i = 0; i < _size; i++) {
            a[i] = new Integer(_values[i]);
        }
        return a;
    }

    // documentation inherited from interface
    public int[] toIntArray ()
    {
        int[] values = new int[_size];
        System.arraycopy(_values, 0, values, 0, _size);
        return values;
    }

    // documentation inherited from interface
    public boolean add (Object o)
    {
        return add(((Integer)o).intValue());
    }

    // documentation inherited from interface
    public boolean add (int value)
    {
        int index = binarySearch(value);
        if (index >= 0) {
            return false;
        }

        // convert the return value into the insertion point
        index += 1;
        index *= -1;

        // expand the values array if necessary, leaving room for the
        // newly added element
        int valen = _values.length;
        int[] source = _values;
        if (valen == _size) {
            _values = new int[valen*2];
            System.arraycopy(source, 0, _values, 0, index);
        }

        // shift and insert
        if (_size > index) {
            System.arraycopy(source, index,
                             _values, index+1, _size-index);
        }
        _values[index] = value;

        // increment our size
        _size += 1;

        return true;
    }

    /**
     * Add all of the values in the supplied array to the set.
     *
     * @param values elements to be added to this set.
     *
     * @return <tt>true</tt> if this set did not already contain all of
     * the specified elements.
     */
    public boolean add (int[] values)
    {
        boolean modified = false;
        int vlength = values.length;
        for (int i = 0; i < vlength; i++) {
            modified = (add(values[i]) || modified);
        }
        return modified;
    }

    // documentation inherited from interface
    public boolean remove (Object o)
    {
        return remove(((Integer)o).intValue());
    }

    // documentation inherited from interface
    public boolean remove (int value)
    {
        int index = binarySearch(value);
        if (index > 0) {
            System.arraycopy(_values, index, _values, index+1, --_size-index);
            _values[_size] = 0;
            return true;
        }
        return false;
    }

    // documentation inherited from interface
    public boolean containsAll (Collection c)
    {
        Iterator iter = c.iterator();
        while (iter.hasNext()) {
            if (!contains(iter.next())) {
                return false;
            }
        }
        return true;
    }

    // documentation inherited from interface
    public boolean addAll (Collection c)
    {
        boolean modified = false;
        Iterator iter = c.iterator();
        while (iter.hasNext()) {
            modified = (add(iter.next()) || modified);
        }
        return modified;
    }

    // documentation inherited from interface
    public boolean retainAll (Collection c)
    {
        throw new UnsupportedOperationException();
    }

    // documentation inherited from interface
    public void clear ()
    {
        Arrays.fill(_values, 0);
        _size = 0;
    }

    // documentation inherited from interface
    public boolean equals (Object o)
    {
        if (o instanceof ArrayIntSet) {
            ArrayIntSet other = (ArrayIntSet)o;
            if (other._size == _size) {
                return Arrays.equals(_values, other._values);
            }
        }

        return false;
    }

    // documentation inherited from interface
    public int hashCode ()
    {
        int hashCode = 0;
        for (int i = 0; i < _size; i++) {
            hashCode ^= _values[i];
        }
        return hashCode;
    }

    /**
     * Performs a binary search on our values array, looking for the
     * specified value. Swiped from <code>java.util.Arrays</code> because
     * those wankers didn't provide a means by which to perform a binary
     * search on a subset of an array.
     */
    protected int binarySearch (int key)
    {
	int low = 0;
	int high = _size-1;

	while (low <= high) {
	    int mid = (low + high) >> 1;
	    int midVal = _values[mid];

	    if (midVal < key) {
		low = mid + 1;
	    } else if (midVal > key) {
		high = mid - 1;
	    } else {
		return mid; // key found
            }
	}

	return -(low + 1);  // key not found.
    }

    /** An array containing the values in this set. */
    protected int[] _values = new int[16];

    /** The number of elements in this set. */
    protected int _size;
}