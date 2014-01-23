/*
 * JARClasspathLocation.java
 * 
 * Copyright (c) 2009, Ralf Biedert All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer. Redistributions in binary form must reproduce the
 * above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of the author nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package net.xeoh.plugins.base.impl.classpath.locator.locations;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import net.xeoh.plugins.base.impl.classpath.cache.JARCache;
import net.xeoh.plugins.base.impl.classpath.cache.JARCache.JARInformation;
import net.xeoh.plugins.base.impl.classpath.locator.AbstractClassPathLocation;

/**
 * Tries to load plugins from a JAR class path location.
 * 
 * @author Ralf Biedert
 */
public class JARClasspathLocation extends AbstractClassPathLocation {

    /**
     * @param cache
     * @param realm
     * @param location
     */
    public JARClasspathLocation(JARCache cache, String realm, URI location) {
        super(cache, realm, location);
    }

    /**
     * @return the location
     */
    public JARInformation getCacheEntry() {
        // If we have a JAR
        if (getType() == LocationType.JAR && this.cache != null)
            this.cacheEntry = this.cache.getJARInformationFor(this.location);

        return this.cacheEntry;
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.base.impl.classpath.locator.AbstractClassPathLocation#getType()
     */
    @Override
    public LocationType getType() {
        return LocationType.JAR;
    }



    /**
     * Returns a list of predefined plugins inside this jar. 
     * 
     * @return .
     */
    public Collection<String> getPredefinedPluginList() {
        return null;
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.base.impl.classpath.locator.AbstractClassPathLocation#listAllEntries()
     */
    @Override
    public Collection<String> listAllEntries() {
        return listAllEntriesFor(this.location);
    }

    /* (non-Javadoc)
     * @see net.xeoh.plugins.base.impl.classpath.locator.AbstractClassPathLocation#listToplevelClassNames()
     */
    @Override
    public Collection<String> listToplevelClassNames() {
        final Collection<String> rval = listToplevelClassNamesForURI(this.location);
        if (this.cacheEntry != null) this.cacheEntry.classesValid = true;
        return rval;
    }

    /**
     * Lists all entries for the given JAR.
     * 
     * @param uri
     * @return .
     */
    public static Collection<String> listAllEntriesFor(URI uri) {
        final Collection<String> rval = new ArrayList<String>();

        try {
            final JarURLConnection connection = (JarURLConnection) new URI("jar:" + uri + "!/").toURL().openConnection();
            final JarFile jarFile = connection.getJarFile();

            final Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                final JarEntry entry = entries.nextElement();

                // We only search for class file entries
                if (entry.isDirectory()) continue;

                String name = entry.getName();

                rval.add(name);
            }
        } catch (MalformedURLException e) {
            Logger.getLogger( JARClasspathLocation.class.getName() ).severe( "list all entries for uri: " + uri.toASCIIString() + " is malformed " );
        } catch (IOException e) {
            Logger.getLogger( JARClasspathLocation.class.getName() ).severe( "list all entries for uri: caught an IOException " + e.toString() );
        } catch (URISyntaxException e) {
            Logger.getLogger( JARClasspathLocation.class.getName() ).severe( "list all entries for uri: uri syntax problem " + e.toString() );
        }
        return rval;
    }

    /**
     * Lists all top level class entries for the given URL.
     * 
     * @param uri
     * @return .
     */
    public static Collection<String> listToplevelClassNamesForURI(URI uri) {
        final Collection<String> rval = new ArrayList<String>();

        // Ensure we have a proper cache entry ...
        //getCacheEntry();

        // Disabled: Not really necessary, is it? And not storing/retrieving these items
        // makes the cache file smaller and saves several ms (~200) loading and storing it
        //if (this.cacheEntry.classesValid) { return this.cacheEntry.classes; }

        try {
            final JarURLConnection connection = (JarURLConnection) new URI("jar:" + uri + "!/").toURL().openConnection();
            final JarFile jarFile = connection.getJarFile();

            final Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                final JarEntry entry = entries.nextElement();

                // We only search for class file entries
                if (entry.isDirectory()) continue;
                if (!entry.getName().endsWith(".class")) continue;

                String name = entry.getName();
                name = name.replaceAll("/", ".");

                // Remove trailing .class 
                if (name.endsWith("class")) {
                    name = name.substring(0, name.length() - 6);
                }

                // Disabled: Not really necessary, is it? And not storing/retrieving these items
                // makes the cache file smaller and saves several ms (~200) loading and storing it
                // Only store something if we have a cache entry
                //if (this.cacheEntry != null) this.cacheEntry.classes.add(name);

                rval.add(name);
            }
        } 
        catch( Exception e )    {
            Logger.getLogger( JARClasspathLocation.class.getName() ).severe( "list top level class names : caught an exception " + e.toString() );
        }

        return rval;
    }
}
