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

/**
 * The pessimist's dream.  This ResultListener silently eats requestCompleted but makes subclasses
 * handle requestFailed.
 */
public abstract class FailureListener<T>
    implements ResultListener<T>
{
    // from interface ResultListener
    public final void requestCompleted (T result)
    {
        // Yeah, yeah, yeah. You did something. Good for you.
    }

    /**
     * Recasts us to look like we're of a different type. We can safely do this because we know
     * that requestCompleted never actually looks at the value passed in.
     */
    public <V> FailureListener<V> retype (Class<V> klass)
    {
        @SuppressWarnings("unchecked") FailureListener<V> casted = (FailureListener<V>)this;
        return casted;
    }
}
