//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
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

import java.io.Serializable;

/**
 * A triple of values that properly implements {@link #hashCode} and {@link #equals}.
 */
public class Triple<A,B,C> implements Serializable
{
    /** The first object. */
    public final A a;

    /** The second object. */
    public final B b;

    /** The third object. */
    public final C c;

    /**
     * Creates a triple with the specified values.
     */
    public static <A, B, C> Triple<A, B, C> newTriple (A a, B b, C c)
    {
        return new Triple<A, B, C>(a, b, c);
    }

    /**
     * Constructs a triple with the specified two objects.
     */
    public Triple (A a, B b, C c)
    {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    @Override // from Object
    public int hashCode ()
    {
        int value = 17;
        value = value * 31 + ((a == null) ? 0 : a.hashCode());
        value = value * 31 + ((b == null) ? 0 : b.hashCode());
        value = value * 31 + ((c == null) ? 0 : c.hashCode());
        return value;
    }

    @Override // from Object
    public boolean equals (Object other)
    {
        if (!(other instanceof Triple<?, ?, ?>)) {
            return false;
        }
        Triple<?, ?, ?> to = (Triple<?, ?, ?>)other;
        return ObjectUtil.equals(a, to.a) && ObjectUtil.equals(b, to.b) &&
            ObjectUtil.equals(c, to.c);
    }

    @Override // from Object
    public String toString ()
    {
        return "[a=" + a + ", b=" + b + ", c=" + c + "]";
    }

    /** Don't you go a changin'. */
    private static final long serialVersionUID = 1;
}
