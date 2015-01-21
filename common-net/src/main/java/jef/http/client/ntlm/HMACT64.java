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

import java.security.MessageDigest;

/**
 * This is an implementation of the HMACT64 keyed hashing algorithm.
 * HMACT64 is defined by Luke Leighton as a modified HMAC-MD5 (RFC 2104)
 * in which the key is truncated at 64 bytes (rather than being hashed
 * via MD5).
 */ 
public class HMACT64 extends MessageDigest implements Cloneable {

    private static final int BLOCK_LENGTH = 64;

    private static final byte IPAD = (byte) 0x36;

    private static final byte OPAD = (byte) 0x5c;

    private MessageDigest md5;

    private byte[] ipad = new byte[BLOCK_LENGTH];

    private byte[] opad = new byte[BLOCK_LENGTH];

    /**
     * Creates an HMACT64 instance which uses the given secret key material.
     *
     * @param key The key material to use in hashing.
     */
    public HMACT64(byte[] key) {
        super("HMACT64");
        int length = Math.min(key.length, BLOCK_LENGTH);
        for (int i = 0; i < length; i++) {
            ipad[i] = (byte) (key[i] ^ IPAD);
            opad[i] = (byte) (key[i] ^ OPAD);
        }
        for (int i = length; i < BLOCK_LENGTH; i++) {
            ipad[i] = IPAD;
            opad[i] = OPAD;
        }
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception ex) {
            throw new IllegalStateException(ex.getMessage());
        }
        engineReset();
    }

    private HMACT64(HMACT64 hmac) throws CloneNotSupportedException {
        super("HMACT64");
        this.ipad = hmac.ipad;
        this.opad = hmac.opad;
        this.md5 = (MessageDigest) hmac.md5.clone();
    }

    public Object clone() {
        try {
            return new HMACT64(this);
        } catch (CloneNotSupportedException ex) {
            throw new IllegalStateException(ex.getMessage());
        }
    }

    protected byte[] engineDigest() {
        byte[] digest = md5.digest();
        md5.update(opad);
        return md5.digest(digest);
    }

    protected int engineDigest(byte[] buf, int offset, int len) {
        byte[] digest = md5.digest();
        md5.update(opad);
        md5.update(digest);
        try {
            return md5.digest(buf, offset, len);
        } catch (Exception ex) {
            throw new IllegalStateException();
        }
    }

    protected int engineGetDigestLength() {
        return md5.getDigestLength();
    }

    protected void engineReset() {
        md5.reset();
        md5.update(ipad);
    }

    protected void engineUpdate(byte b) {
        md5.update(b);
    }

    protected void engineUpdate(byte[] input, int offset, int len) {
        md5.update(input, offset, len);
    }

}
