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

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides a mechanism for converting a string representation of a value
 * into a Java object when provided with the type of the target object.
 * This is used to do things like populate object fields with values
 * parsed from an XML file and the like.
 */
public class ValueMarshaller
{
    /**
     * Attempts to convert the specified value to an instance of the
     * specified object type.
     *
     * @exception Exception thrown if no field parser exists for the
     * target type or if an error occurs while parsing the value.
     */
    public static Object unmarshal (Class<?> type, String source)
        throws Exception
    {
        if (type.isEnum()) {
            @SuppressWarnings("unchecked") // we just asked type if it was an enum...
            Class<? extends Enum> etype = (Class<? extends Enum>)type;
            @SuppressWarnings("unchecked") // silly compiler, we're assigning to Object
            Object o = Enum.valueOf(etype, source); // may throw an exception
            return o;
        }
        // look up an argument parser for the field type
        Parser parser = _parsers.get(type);
        if (parser == null) {
            String errmsg = "Don't know how to convert strings into " +
                "values of type '" + type + "'.";
            throw new Exception(errmsg);
        }
        return parser.parse(source);
    }

    protected static interface Parser
    {
        public Object parse (String source) throws Exception;
    }

    protected static Map<Class<?>,Parser> _parsers = new HashMap<Class<?>,Parser>();
    static {
        Parser p;
        // we can parse strings
        _parsers.put(String.class, new Parser() {
            public Object parse (String source) throws Exception {
                return source;
            }
        });

        // and bytes
        p = new Parser() {
            public Object parse (String source) throws Exception {
                return Byte.valueOf(source);
            }
        };
        _parsers.put(Byte.class, p);
        _parsers.put(Byte.TYPE, p);

        // and shorts
        p = new Parser() {
            public Object parse (String source) throws Exception {
                return Short.valueOf(source);
            }
        };
        _parsers.put(Short.class, p);
        _parsers.put(Short.TYPE, p);

        // and ints
        p = new Parser() {
            public Object parse (String source) throws Exception {
                return Integer.valueOf(source);
            }
        };
        _parsers.put(Integer.class, p);
        _parsers.put(Integer.TYPE, p);

        // and longs
        p = new Parser() {
            public Object parse (String source) throws Exception {
                return Long.valueOf(source);
            }
        };
        _parsers.put(Long.class, p);
        _parsers.put(Long.TYPE, p);

        // and floats
        p = new Parser() {
            public Object parse (String source) throws Exception {
                return Float.valueOf(source);
            }
        };
        _parsers.put(Float.class, p);
        _parsers.put(Float.TYPE, p);

        // and booleans
        p = new Parser() {
            public Object parse (String source) throws Exception {
                return Boolean.valueOf(source);
            }
        };
        _parsers.put(Boolean.class, p);
        _parsers.put(Boolean.TYPE, p);

        // and byte arrays
        _parsers.put(byte[].class, new Parser() {
            public Object parse (String source) throws Exception {
                String[] strs = StringUtil.parseStringArray(source);
                int count = strs.length;
                byte[] bytes = new byte[count];
                for (int ii = 0; ii < count; ii++) {
                    bytes[ii] = Byte.valueOf(strs[ii]);
                }
                return bytes;
            }
        });

        // and int arrays
        _parsers.put(int[].class, new Parser() {
            public Object parse (String source) throws Exception {
                return StringUtil.parseIntArray(source);
            }
        });

        // and float arrays
        _parsers.put(float[].class, new Parser() {
            public Object parse (String source) throws Exception {
                return StringUtil.parseFloatArray(source);
            }
        });

        // and string arrays, oh my!
        _parsers.put(String[].class, new Parser() {
            public Object parse (String source) throws Exception {
                return StringUtil.parseStringArray(source);
            }
        });

        // and Color objects
        _parsers.put(Color.class, new Parser() {
            public Object parse (String source) throws Exception {
                if (source.startsWith("#")) {
                    source = source.substring(1);
                }
                return new Color(Integer.parseInt(source, 16));
            }
        });
    }
}
