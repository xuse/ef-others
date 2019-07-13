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

import jef.http.client.support.HttpException;
import jef.net.AuthenticationException;
import jef.tools.Exceptions;
import jef.tools.support.JefBase64;


final class NTLM {
    /**
     * Returns the response for the given message.
     *
     * @param message the message that was received from the server.
     * @param username the username to authenticate with.
     * @param password the password to authenticate with.
     * @param host The host.
     * @param domain the NT domain to authenticate in.
     * @return The response.
     * @throws HttpException If the messages cannot be retrieved.
     */
    public final String getResponseFor(String message,String username, String password, String host, String domain)
            throws AuthenticationException {
                
        final String response;
        if (message == null) {
            response = getType1Message(host, domain);
        } else {
            response = getType3Message(username, password, host, domain,message);
        }
        return response;
    }

    /**
     * Creates the first message (type 1 message) in the NTLM authentication sequence.
     * This message includes the user name, domain and host for the authentication session.
     *
     * @param host the computer name of the host requesting authentication.
     * @param domain The domain to authenticate with.
     * @return String the message to add to the HTTP request header.
     */
    public String getType1Message(String host, String domain) {
    	Type1Message type1=new Type1Message(0,domain,host);
        return encode(type1.toByteArray());
    }

    /** 
     * Creates the type 3 message using the given server nonce.  The type 3 message includes all the
     * information for authentication, host, domain, username and the result of encrypting the
     * nonce sent by the server using the user's password as the key.
     *
     * @param user The user name.  This should not include the domain name.
     * @param password The password.
     * @param host The host that is originating the authentication request.
     * @param domain The domain to authenticate within.
     * @param nonce the 8 byte array the server sent.
     * @return The type 3 message.
     * @throws AuthenticationException If {@encrypt(byte[],byte[])} fails.
     */
    public String getType3Message(String user, String password,String host, String domain, String alldata)
    throws AuthenticationException {
    	try {
			Type2Message type2=new Type2Message(JefBase64.decodeFast(alldata));
			Type3Message type3=new Type3Message(type2,password,domain,user,host,0);
	        return encode(type3.toByteArray());
		} catch (IOException e) {
			Exceptions.log(e);
	        return "Error";
		}
    }

    // "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
    private static final byte[] ENCODE_TABLE = new byte[] { 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119,
    	120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 43, 47 };

	/**
	 * 不带换行的Base64编码 
	 * @param data
	 * @return
	 */
	public static String encode(byte[] data) {
		if (data == null)
			return null;

		int fullGroups = data.length / 3;
		int resultBytes = fullGroups * 4;
		if (data.length % 3 != 0)
			resultBytes += 4;

		byte[] result = new byte[resultBytes];
		int resultIndex = 0;
		int dataIndex = 0;
		int temp = 0;
		for (int i = 0; i < fullGroups; i++) {
			temp = (data[dataIndex++] & 0xff) << 16 | (data[dataIndex++] & 0xff) << 8 | data[dataIndex++] & 0xff;

			result[resultIndex++] = ENCODE_TABLE[(temp >> 18) & 0x3f];
			result[resultIndex++] = ENCODE_TABLE[(temp >> 12) & 0x3f];
			result[resultIndex++] = ENCODE_TABLE[(temp >> 6) & 0x3f];
			result[resultIndex++] = ENCODE_TABLE[temp & 0x3f];
		}
		temp = 0;
		while (dataIndex < data.length) {
			temp <<= 8;
			temp |= data[dataIndex++] & 0xff;
		}
		switch (data.length % 3) {
		case 1:
			temp <<= 8;
			temp <<= 8;
			result[resultIndex++] = ENCODE_TABLE[(temp >> 18) & 0x3f];
			result[resultIndex++] = ENCODE_TABLE[(temp >> 12) & 0x3f];
			result[resultIndex++] = 0x3D;
			result[resultIndex++] = 0x3D;
			break;
		case 2:
			temp <<= 8;
			result[resultIndex++] = ENCODE_TABLE[(temp >> 18) & 0x3f];
			result[resultIndex++] = ENCODE_TABLE[(temp >> 12) & 0x3f];
			result[resultIndex++] = ENCODE_TABLE[(temp >> 6) & 0x3f];
			result[resultIndex++] = 0x3D;
			break;
		default:
			break;
		}
		return new String(result,0,resultIndex);
	}
}
