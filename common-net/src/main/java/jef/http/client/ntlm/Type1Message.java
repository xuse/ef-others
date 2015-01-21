/*
 * JEF - Copyright 2009-2010 Jiyi (mr.jiyi@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jef.http.client.ntlm;

import java.io.IOException;


/**
 * Represents an NTLMSSP Type-1 message.
 */
public class Type1Message extends NtlmMessage {

    private static final int DEFAULT_FLAGS;

    private static final String DEFAULT_DOMAIN;

    private static final String DEFAULT_WORKSTATION;

    private String suppliedDomain;

    private String suppliedWorkstation;

    static {
        DEFAULT_FLAGS = NTLMSSP_NEGOTIATE_NTLM |
                (Config.getBoolean("jcifs.smb.client.useUnicode", true) ?
                        NTLMSSP_NEGOTIATE_UNICODE : NTLMSSP_NEGOTIATE_OEM);
        DEFAULT_DOMAIN = Config.getProperty("jcifs.smb.client.domain", null);
        DEFAULT_WORKSTATION = "";
    }

    /**
     * Creates a Type-1 message using default values from the current
     * environment.
     */
    public Type1Message() {
        this(getDefaultFlags(), getDefaultDomain(), getDefaultWorkstation());
    }

    /**
     * Creates a Type-1 message with the specified parameters.
     *
     * @param flags The flags to apply to this message.
     * @param suppliedDomain The supplied authentication domain.
     * @param suppliedWorkstation The supplied workstation name.
     */
    public Type1Message(int flags, String suppliedDomain,
            String suppliedWorkstation) {
        setFlags(getDefaultFlags() | flags);
        setSuppliedDomain(suppliedDomain);
        if (suppliedWorkstation == null)
            suppliedWorkstation = getDefaultWorkstation();
        setSuppliedWorkstation(suppliedWorkstation);
    }

    /**
     * Creates a Type-1 message using the given raw Type-1 material.
     *
     * @param material The raw Type-1 material used to construct this message.
     * @throws IOException If an error occurs while parsing the material.
     */
    public Type1Message(byte[] material) throws IOException {
        parse(material);
    }

    /**
     * Returns the supplied authentication domain.
     *
     * @return A <code>String</code> containing the supplied domain.
     */
    public String getSuppliedDomain() {
        return suppliedDomain;
    }

    /**
     * Sets the supplied authentication domain for this message.
     *
     * @param suppliedDomain The supplied domain for this message.
     */
    public void setSuppliedDomain(String suppliedDomain) {
        this.suppliedDomain = suppliedDomain;
    }

    /**
     * Returns the supplied workstation name.
     * 
     * @return A <code>String</code> containing the supplied workstation name.
     */
    public String getSuppliedWorkstation() {
        return suppliedWorkstation;
    }

    /**
     * Sets the supplied workstation name for this message.
     * 
     * @param suppliedWorkstation The supplied workstation for this message.
     */
    public void setSuppliedWorkstation(String suppliedWorkstation) {
        this.suppliedWorkstation = suppliedWorkstation;
    }

    public byte[] toByteArray() {
        try {
            String suppliedDomain = getSuppliedDomain();
            String suppliedWorkstation = getSuppliedWorkstation();
            int flags = getFlags();
            boolean hostInfo = false;
            byte[] domain = new byte[0];
            if (suppliedDomain != null && suppliedDomain.length() != 0) {
                hostInfo = true;
                flags |= NTLMSSP_NEGOTIATE_OEM_DOMAIN_SUPPLIED;
                domain = suppliedDomain.toUpperCase().getBytes(
                        getOEMEncoding());
            } else {
                flags &= (NTLMSSP_NEGOTIATE_OEM_DOMAIN_SUPPLIED ^ 0xffffffff);
            }
            byte[] workstation = new byte[0];
            if (suppliedWorkstation != null &&
                    suppliedWorkstation.length() != 0) {
                hostInfo = true;
                flags |= NTLMSSP_NEGOTIATE_OEM_WORKSTATION_SUPPLIED;
                workstation =
                        suppliedWorkstation.toUpperCase().getBytes(
                                getOEMEncoding());
            } else {
                flags &= (NTLMSSP_NEGOTIATE_OEM_WORKSTATION_SUPPLIED ^
                        0xffffffff);
            }
            byte[] type1 = new byte[hostInfo ?
                    (32 + domain.length + workstation.length) : 16];
            System.arraycopy(NTLMSSP_SIGNATURE, 0, type1, 0, 8);
            writeULong(type1, 8, 1);
            writeULong(type1, 12, flags);
            if (hostInfo) {
                writeSecurityBuffer(type1, 16, 32, domain);
                writeSecurityBuffer(type1, 24, 32 + domain.length, workstation);
            }
            return type1;
        } catch (IOException ex) {
            throw new IllegalStateException(ex.getMessage());
        }
    }

    public String toString() {
        String suppliedDomain = getSuppliedDomain();
        String suppliedWorkstation = getSuppliedWorkstation();
        return "Type1Message[suppliedDomain=" + (suppliedDomain == null ? "null" : suppliedDomain) +
                ",suppliedWorkstation=" + (suppliedWorkstation == null ? "null" : suppliedWorkstation) +
                ",flags=0x" + Integer.toOctalString(getFlags()) + "]";
    }

    /**
     * Returns the default flags for a generic Type-1 message in the
     * current environment.
     * 
     * @return An <code>int</code> containing the default flags.
     */
    public static int getDefaultFlags() {
        return DEFAULT_FLAGS;
    }

    /**
     * Returns the default domain from the current environment.
     *
     * @return A <code>String</code> containing the default domain.
     */
    public static String getDefaultDomain() {
        return DEFAULT_DOMAIN;
    }

    /**
     * Returns the default workstation from the current environment.
     *
     * @return A <code>String</code> containing the default workstation.
     */
    public static String getDefaultWorkstation() {
        return DEFAULT_WORKSTATION;
    }

    private void parse(byte[] material) throws IOException {
        for (int i = 0; i < 8; i++) {
            if (material[i] != NTLMSSP_SIGNATURE[i]) {
                throw new IOException("Not an NTLMSSP message.");
            }
        }
        if (readULong(material, 8) != 1) {
            throw new IOException("Not a Type 1 message.");
        }
        int flags = readULong(material, 12);
        String suppliedDomain = null;
        if ((flags & NTLMSSP_NEGOTIATE_OEM_DOMAIN_SUPPLIED) != 0) {
            byte[] domain = readSecurityBuffer(material, 16);
            suppliedDomain = new String(domain, getOEMEncoding());
        }
        String suppliedWorkstation = null;
        if ((flags & NTLMSSP_NEGOTIATE_OEM_WORKSTATION_SUPPLIED) != 0) {
            byte[] workstation = readSecurityBuffer(material, 24);
            suppliedWorkstation = new String(workstation, getOEMEncoding());
        }
        setFlags(flags);
        setSuppliedDomain(suppliedDomain);
        setSuppliedWorkstation(suppliedWorkstation);
    }

}
