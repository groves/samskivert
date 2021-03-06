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

package com.samskivert.jdbc;

import java.sql.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.samskivert.Log.log;

/**
 * The liaison registry provides access to the appropriate database liaison implementation for a
 * particular database connection.
 */
public class LiaisonRegistry
{
    /**
     * Fetch the appropriate database liaison for the supplied URL, which should be the same string
     * that would be used to configure a connection to the database.
     */
    public static DatabaseLiaison getLiaison (String url)
    {
        // see if we already have a liaison mapped for this connection
        DatabaseLiaison liaison = _mappings.get(url);
        if (liaison == null) {
            // scan the list looking for a matching liaison
            for (DatabaseLiaison candidate : _liaisons) {
                if (candidate.matchesURL(url)) {
                    liaison = candidate;
                    break;
                }
            }

            // if we didn't find a matching liaison, use the default
            if (liaison == null) {
                log.warning("Unable to match liaison for database. Using default.", "url", url);
                liaison = new DefaultLiaison();
            }

            // map this URL to this liaison
            _mappings.put(url, liaison);
        }

        return liaison;
    }

    /**
     * Fetch the appropriate database liaison for the supplied database connection.
     */
    public static DatabaseLiaison getLiaison (Connection conn)
        throws SQLException
    {
        return getLiaison(conn.getMetaData().getURL());
    }

    protected static void registerLiaisonClass (Class<? extends DatabaseLiaison> lclass)
    {
        // create a new instance and stick it on our list
        try {
            _liaisons.add(lclass.newInstance());
        } catch (Exception e) {
            log.warning("Unable to instantiate liaison", "class", lclass.getName(), "error", e);
        }
    }

    protected static ArrayList<DatabaseLiaison> _liaisons = new ArrayList<DatabaseLiaison>();
    protected static Map<String,DatabaseLiaison> _mappings = new HashMap<String,DatabaseLiaison>();

    // register our liaison classes
    static {
        registerLiaisonClass(MySQLLiaison.class);
        registerLiaisonClass(PostgreSQLLiaison.class);
        registerLiaisonClass(HsqldbLiaison.class);
    }
}
