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

package com.samskivert.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

/**
 * A {@link JPanel} extension that implements the {@link Scrollable}
 * interface, instructing it to scroll partially obscured components into
 * view if scrolling is done in the direction of a partially obscured
 * component. If no component is partially obscured, the next component is
 * scrolled into view in its entirety. Presently the code only works with
 * vertically laid out components, horizontal scrolling simply moves by a
 * default number of pixels every time.
 */
public class ScrollablePanel extends JPanel
    implements Scrollable
{
    /**
     * Pass through constructor.
     */
    public ScrollablePanel (LayoutManager layout, boolean isDoubleBuffered)
    {
        super(layout, isDoubleBuffered);
    }

    /**
     * Pass through constructor.
     */
    public ScrollablePanel (LayoutManager layout)
    {
        super(layout);
    }

    /**
     * Pass through constructor.
     */
    public ScrollablePanel (boolean isDoubleBuffered)
    {
        super(isDoubleBuffered);
    }

    /**
     * Pass through constructor.
     */
    public ScrollablePanel ()
    {
    }

    /**
     * Instructs this panel to not scroll in the horizontal direction but
     * to set its viewport's horizontal size when it is sized.
     */
    public void setTracksViewportWidth (boolean tracksWidth)
    {
        _tracksWidth = tracksWidth;
    }

    /**
     * Instructs this panel to not scroll in the vertical direction but to
     * set its viewport's vertical size when it is sized.
     */
    public void setTracksViewportHeight (boolean tracksHeight)
    {
        _tracksHeight = tracksHeight;
    }

    /**
     * Set the unit scroll increment.
     */
    public void setUnitScrollIncrement (int inc)
    {
        _unitScroll = inc;
    }

    // documentation inherited from interface
    public Dimension getPreferredScrollableViewportSize ()
    {
        return getPreferredSize();
    }

    // documentation inherited from interface
    public int getScrollableUnitIncrement (
        Rectangle visibleRect, int orientation, int direction)
    {
        if (_unitScroll != -1) {
            return _unitScroll;
        }

        Insets insets = getInsets();

        if (orientation == SwingConstants.HORIZONTAL) {
            // nothing sensible to do here

        } else {
            if (direction > 0) {
                int rectbot = visibleRect.y + visibleRect.height;
                Component comp = getComponentAt(insets.left, rectbot);

                // if there's no component at the edge, drop down 5
                // pixels and look again
                if (comp == this) {
                    comp = getComponentAt(insets.left, rectbot+5);
                }

                if (comp != this && comp != null) {
                    int compbot = comp.getY() + comp.getHeight();
                    return compbot - rectbot + 5;
                }

            } else {
                Component comp = getComponentAt(insets.left, visibleRect.y);

                // if there's no component at the edge, move up 5
                // pixels and look again
                if (comp == this) {
                    comp = getComponentAt(insets.left, visibleRect.y-5);
                }

                if (comp != this && comp != null) {
                    return visibleRect.y - comp.getY() + 5;
                }
            }
        }

        return DEFAULT_SCROLL_AMOUNT;
    }

    // documentation inherited from interface
    public int getScrollableBlockIncrement (
        Rectangle visibleRect, int orientation, int direction)
    {
        if (orientation == SwingConstants.HORIZONTAL) {
            return visibleRect.width;
        } else {
            return visibleRect.height;
        }
    }

    // documentation inherited from interface
    public boolean getScrollableTracksViewportWidth ()
    {
        return _tracksWidth;
    }

    // documentation inherited from interface
    public boolean getScrollableTracksViewportHeight ()
    {
        return _tracksHeight;
    }

    protected boolean _tracksWidth = false;
    protected boolean _tracksHeight = false;

    protected int _unitScroll = -1;

    /** The number of pixels to scroll if we can't find a component to
     * scroll into view or if we're scrolling horizontally. */
    protected static final int DEFAULT_SCROLL_AMOUNT = 10;
}
