//
// $Id: JDBCTableSiteIdentifier.java,v 1.7 2003/11/13 16:58:39 mdb Exp $
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

package com.samskivert.servlet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import com.samskivert.io.PersistenceException;

import com.samskivert.jdbc.ConnectionProvider;
import com.samskivert.jdbc.DatabaseLiaison;
import com.samskivert.jdbc.JDBCUtil;
import com.samskivert.jdbc.SimpleRepository;

import com.samskivert.util.ArrayUtil;
import com.samskivert.util.HashIntMap;
import com.samskivert.util.StringUtil;

import com.samskivert.Log;

/**
 * Accomplishes the process of site identification based on a mapping from
 * domains (e.g. samskivert.com) to site identifiers that is maintained in
 * a database table, accessible via JDBC (hence the name).
 *
 * <p> There are two tables, one that maps domains to site identifiers and
 * another that maps site identifiers to site strings. These are both
 * loaded at construct time and refreshed periodically in the course of
 * normal operation.
 */
public class JDBCTableSiteIdentifier implements SiteIdentifier
{
    /** The database identifier used to obtain a connection from our
     * connection provider. The value is <code>sitedb</code> which you'll
     * probably need to know to provide the proper configuration to your
     * connection provider. */
    public static final String SITE_IDENTIFIER_IDENT = "sitedb";

    /**
     * Constructs a JDBC table site identifier with the supplied
     * connection provider from which to obtain its database connection.
     *
     * @see #SITE_IDENTIFIER_IDENT
     */
    public JDBCTableSiteIdentifier (ConnectionProvider conprov)
        throws PersistenceException
    {
        _repo = new SiteIdentifierRepository(conprov);
        // load up our site data
        _repo.refreshSiteData();
    }

    // documentation inherited
    public int identifySite (HttpServletRequest req)
    {
        String serverName = req.getServerName();

        // scan for the mapping that matches the specified domain
        int msize = _mappings.size();
        for (int i = 0; i < msize; i++) {
            SiteMapping mapping = (SiteMapping)_mappings.get(i);
            if (serverName.endsWith(mapping.domain)) {
                return mapping.siteId;
            }
        }

        // if we matched nothing, return the default id
        return DEFAULT_SITE_ID;
    }

    // documentation inherited
    public String getSiteString (int siteId)
    {
        Site site = (Site)_sitesById.get(siteId);
        return (site == null) ? DEFAULT_SITE_STRING : site.siteString;
    }

    // documentation inherited
    public int getSiteId (String siteString)
    {
        Site site = (Site)_sitesByString.get(siteString);
        return (site == null) ? DEFAULT_SITE_ID : site.siteId;
    }

    // documentation inherited from interface
    public Iterator enumerateSites ()
    {
        return _sitesById.values().iterator();
    }

    /**
     * Insert a new site into the site table and into this mapping.
     */
    public Site insertNewSite (String siteString)
        throws PersistenceException
    {
        if (_sitesByString.containsKey(siteString)) {
            return null;
        }

        // add it to the db
        Site site = new Site();
        site.siteString = siteString;
        _repo.insertNewSite(site);

        // add it to our two mapping tables
        _sitesById.put(site.siteId, site);
        _sitesByString.put(site.siteString, site);

        return site;
    }


    /**
     * Used to load information from the site database.
     */
    protected class SiteIdentifierRepository extends SimpleRepository
        implements SimpleRepository.Operation
    {
        public SiteIdentifierRepository (ConnectionProvider conprov)
        {
            super(conprov, SITE_IDENTIFIER_IDENT);
        }

        public void refreshSiteData ()
            throws PersistenceException
        {
            // we are the operation!
            execute(this);
        }

        public Object invoke (Connection conn, DatabaseLiaison liaison)
            throws PersistenceException, SQLException
        {
            Statement stmt = conn.createStatement();
            try {
                // first load up the list of sites
                String query = "select siteId, stringId from sites";
                ResultSet rs = stmt.executeQuery(query);
                HashIntMap sites = new HashIntMap();
                HashMap strings = new HashMap();
                while (rs.next()) {
                    Site site = new Site(rs.getInt(1), rs.getString(2));
                    sites.put(site.siteId, site);
                    strings.put(site.siteString, site);
                }
                _sitesById = sites;
                _sitesByString = strings;

                // now load up the domain mappings
                query = "select domain, siteId from domains";
                rs = stmt.executeQuery(query);
                ArrayList mappings = new ArrayList();
                while (rs.next()) {
                    mappings.add(new SiteMapping(rs.getInt(2),
                                                 rs.getString(1)));
                }
                _mappings = mappings;

                // sort the mappings in order of specificity
                Collections.sort(_mappings);
//                 Log.info("Loaded site mappings " +
//                          StringUtil.toString(_mappings) + ".");

                // nothing to return
                return null;

            } finally {
                JDBCUtil.close(stmt);
            }
        }

        /**
         * Add a new site to the database.
         */
        public void insertNewSite (final Site site)
            throws PersistenceException
        {
            execute(new Operation() {
                public Object invoke (Connection conn, DatabaseLiaison liaison)
                    throws PersistenceException, SQLException
                {
                    PreparedStatement stmt = null;
                    try {
                        stmt = conn.prepareStatement(
                            "insert into sites (stringId) VALUES (?)");
                        stmt.setString(1, site.siteString);
                        if (1 != stmt.executeUpdate()) {
                            throw new PersistenceException(
                                "Not inserted " + site);
                        }
                        site.siteId = liaison.lastInsertedId(conn);

                    } finally {
                        JDBCUtil.close(stmt);
                    }

                    return null;
                }
            });
        }
    }

    /**
     * Used to track domain to site identifier mappings.
     */
    protected static class SiteMapping implements Comparable
    {
        /** The domain to match. */
        public String domain;

        /** The site identifier for the associated domain. */
        public int siteId;

        public SiteMapping (int siteId, String domain)
        {
            this.siteId = siteId;
            this.domain = domain;
            byte[] bytes = domain.getBytes();
            ArrayUtil.reverse(bytes);
            _rdomain = new String(bytes);
        }

        /**
         * Site mappings sort from most specific (www.yahoo.com) to least
         * specific (yahoo.com).
         */
        public int compareTo (Object other)
        {
            if (other instanceof SiteMapping) {
                SiteMapping orec = (SiteMapping)other;
                return orec._rdomain.compareTo(_rdomain);
            } else {
                // no comparablo
                return getClass().getName().compareTo(
                    other.getClass().getName());
            }
        }

        /** Returns a string representation of this site mapping. */
        public String toString ()
        {
            return "[" + domain + " => " + siteId + "]";
        }

        protected String _rdomain;
    }

    /** The repository through which we load up site identifier
     * information. */
    protected SiteIdentifierRepository _repo;

    /** The list of domain to site identifier mappings ordered from most
     * specific domain to least specific. */
    protected ArrayList _mappings = new ArrayList();

    /** The mapping from integer site identifiers to string site
     * identifiers. */
    protected HashIntMap _sitesById = new HashIntMap();

    /** The mapping from string site identifiers to integer site
     * identifiers. */
    protected HashMap _sitesByString = new HashMap();
}
