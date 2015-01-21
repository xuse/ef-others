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

/**
 * Flags used during negotiation of NTLMSSP authentication.
 */
public interface NtlmFlags {

    /**
    * Indicates whether Unicode strings are supported or used.
    */
    public static final int NTLMSSP_NEGOTIATE_UNICODE = 0x00000001;

    /**
    * Indicates whether OEM strings are supported or used.
    */
    public static final int NTLMSSP_NEGOTIATE_OEM = 0x00000002;

    /**
    * Indicates whether the authentication target is requested from
    * the server.
    */
    public static final int NTLMSSP_REQUEST_TARGET = 0x00000004;

    /**
    * Specifies that communication across the authenticated channel
    * should carry a digital signature (message integrity).
    */
    public static final int NTLMSSP_NEGOTIATE_SIGN = 0x00000010;

    /**
    * Specifies that communication across the authenticated channel
    * should be encrypted (message confidentiality).
    */
    public static final int NTLMSSP_NEGOTIATE_SEAL = 0x00000020;

    /**
    * Indicates datagram authentication.
    */
    public static final int NTLMSSP_NEGOTIATE_DATAGRAM_STYLE = 0x00000040;

    /**
    * Indicates that the LAN Manager session key should be used for
    * signing and sealing authenticated communication.
    */
    public static final int NTLMSSP_NEGOTIATE_LM_KEY = 0x00000080;

    public static final int NTLMSSP_NEGOTIATE_NETWARE = 0x00000100;

    /**
    * Indicates support for NTLM authentication.
    */
    public static final int NTLMSSP_NEGOTIATE_NTLM = 0x00000200;

    /**
    * Indicates whether the OEM-formatted domain name in which the
    * client workstation has membership is supplied in the Type-1 message.
    * This is used in the negotation of local authentication. 
    */
    public static final int NTLMSSP_NEGOTIATE_OEM_DOMAIN_SUPPLIED =
            0x00001000;

    /**
    * Indicates whether the OEM-formatted workstation name is supplied
    * in the Type-1 message.  This is used in the negotiation of local
    * authentication.
    */
    public static final int NTLMSSP_NEGOTIATE_OEM_WORKSTATION_SUPPLIED =
            0x00002000;

    /**
    * Sent by the server to indicate that the server and client are
    * on the same machine.  This implies that the server will include
    * a local security context handle in the Type 2 message, for
    * use in local authentication.
    */
    public static final int NTLMSSP_NEGOTIATE_LOCAL_CALL = 0x00004000;

    /**
    * Indicates that authenticated communication between the client
    * and server should carry a "dummy" digital signature.
    */
    public static final int NTLMSSP_NEGOTIATE_ALWAYS_SIGN = 0x00008000;

    /**
    * Sent by the server in the Type 2 message to indicate that the 
    * target authentication realm is a domain.
    */
    public static final int NTLMSSP_TARGET_TYPE_DOMAIN = 0x00010000;

    /**
    * Sent by the server in the Type 2 message to indicate that the 
    * target authentication realm is a server.
    */
    public static final int NTLMSSP_TARGET_TYPE_SERVER = 0x00020000;

    /**
    * Sent by the server in the Type 2 message to indicate that the 
    * target authentication realm is a share (presumably for share-level
    * authentication).
    */
    public static final int NTLMSSP_TARGET_TYPE_SHARE = 0x00040000;

    /**
    * Indicates that the NTLM2 signing and sealing scheme should be used
    * for protecting authenticated communications.  This refers to a
    * particular session security scheme, and is not related to the use
    * of NTLMv2 authentication.
    */ 
    public static final int NTLMSSP_NEGOTIATE_NTLM2 = 0x00080000;

    public static final int NTLMSSP_REQUEST_INIT_RESPONSE = 0x00100000;

    public static final int NTLMSSP_REQUEST_ACCEPT_RESPONSE = 0x00200000;

    public static final int NTLMSSP_REQUEST_NON_NT_SESSION_KEY = 0x00400000;

    /**
    * Sent by the server in the Type 2 message to indicate that it is
    * including a Target Information block in the message.  The Target
    * Information block is used in the calculation of the NTLMv2 response.
    */
    public static final int NTLMSSP_NEGOTIATE_TARGET_INFO = 0x00800000;

    /**
    * Indicates that 128-bit encryption is supported.
    */
    public static final int NTLMSSP_NEGOTIATE_128 = 0x20000000;

    public static final int NTLMSSP_NEGOTIATE_KEY_EXCH = 0x40000000;

    /**
    * Indicates that 56-bit encryption is supported.
    */
    public static final int NTLMSSP_NEGOTIATE_56 = 0x80000000;

}
