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

import java.util.concurrent.Callable;

import com.samskivert.annotation.ReplacedBy;

/**
 * Utilities for Callables.
 */
public class Callables
{
    /**
     * Return a {@link Callable} that merely returns the specified value.
     * No exception will ever be thrown.
     */
    @ReplacedBy("com.google.common.util.concurrent.Callables#returning()")
    public static <V> Callable<V> asCallable (final V value)
    {
        return new Callable<V>() {
            public V call () {
                return value;
            }
        };
    }
}
