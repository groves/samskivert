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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Point;
import java.awt.Rectangle;

import java.util.HashMap;

import static com.samskivert.Log.log;

/**
 * Used to lay out components at absolute coordinates. This layout manager
 * will not gracefully deal with not having room to layout the components
 * at the coordinates specified, so be sure only to use it in very
 * controlled circumstances.
 *
 * <p> Components should be added with a {@link Point} or {@link
 * Rectangle} object as their constraints. If their constraints are a
 * {@link Point} they will be laid out at those coordinates with their
 * preferred size. If it is a {@link Rectangle} they will be laid out in
 * those exact bounds.
 */
public class AbsoluteLayout
    implements LayoutManager2
{
    // documentation inherited from interface
    public void addLayoutComponent (String name, Component comp)
    {
        throw new RuntimeException("You must use " +
                                   "addLayoutComponent(Component, Object)");
    }

    // documentation inherited from interface
    public void addLayoutComponent (Component comp, Object constraints)
    {
        if (!(constraints instanceof Point ||
              constraints instanceof Rectangle)) {
            throw new IllegalArgumentException("Constraints must be " +
                                               "Point or Rectangle");
        }
        _constraints.put(comp, constraints);
    }

    // documentation inherited from interface
    public void removeLayoutComponent (Component comp)
    {
        _constraints.remove(comp);
    }

    // documentation inherited from interface
    public Dimension preferredLayoutSize (Container parent)
    {
        Rectangle rect = new Rectangle();
        Rectangle temp = new Rectangle();
        int pcount = parent.getComponentCount();
        for (int ii = 0; ii < pcount; ii++) {
            Component comp = parent.getComponent(ii);
            if (!comp.isVisible()) {
                continue;
            }

            Object constr = _constraints.get(comp);
            if (constr == null) {
                log.warning("No constraints for child!?", "cont", parent, "comp", comp);
                continue;
            }

            if (constr instanceof Rectangle) {
                rect.add((Rectangle)constr);
            } else {
                Point p = (Point)constr;
                Dimension d = comp.getPreferredSize();
                temp.setBounds(p.x, p.y, d.width, d.height);
                rect.add(temp);
            }
        }

        Dimension dims = new Dimension(rect.width, rect.height);

        // account for the insets
        Insets insets = parent.getInsets();
        dims.width += insets.left + insets.right;
        dims.height += insets.top + insets.bottom;

        return dims;
    }

    // documentation inherited from interface
    public Dimension minimumLayoutSize (Container parent)
    {
        // we don't do no fancy business
        return preferredLayoutSize(parent);
    }

    // documentation inherited from interface
    public Dimension maximumLayoutSize (Container parent)
    {
        // we don't do no fancy business
        return preferredLayoutSize(parent);
    }

    // documentation inherited from interface
    public void layoutContainer (Container parent)
    {
        Insets insets = parent.getInsets();
        int pcount = parent.getComponentCount();
        for (int ii = 0; ii < pcount; ii++) {
            Component comp = parent.getComponent(ii);
            if (!comp.isVisible()) {
                continue;
            }

            Object constr = _constraints.get(comp);
            if (constr == null) {
                log.warning("No constraints for child!?", "cont", parent, "comp", comp);
                continue;
            }

            if (constr instanceof Rectangle) {
                Rectangle r = (Rectangle)constr;
                comp.setBounds(insets.left + r.x, insets.top + r.y,
                               r.width, r.height);

            } else {
                Point p = (Point)constr;
                Dimension d = comp.getPreferredSize();
                comp.setBounds(insets.left + p.x, insets.top + p.y,
                               d.width, d.height);
            }
        }
    }

    // documentation inherited from interface
    public void invalidateLayout (Container target)
    {
        // nothing to do here
    }

    // documentation inherited from interface
    public float getLayoutAlignmentX (Container target)
    {
        return 0;
    }

    // documentation inherited from interface
    public float getLayoutAlignmentY (Container target)
    {
        return 0;
    }

    protected HashMap<Component,Object> _constraints =
        new HashMap<Component,Object>();
}
