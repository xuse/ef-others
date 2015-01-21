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

public class RC4 {

	byte[] s;
	int i, j;

	public RC4() {
	}

	public RC4(byte[] key) {
		init(key, 0, key.length);
	}

	public void init(byte[] key, int ki, int klen) {
		s = new byte[256];

		for (i = 0; i < 256; i++)
			s[i] = (byte) i;

		for (i = j = 0; i < 256; i++) {
			j = (j + key[ki + i % klen] + s[i]) & 0xff;
			byte t = s[i];
			s[i] = s[j];
			s[j] = t;
		}

		i = j = 0;
	}

	public void update(byte[] src, int soff, int slen, byte[] dst, int doff) {
		int slim;

		slim = soff + slen;
		while (soff < slim) {
			i = (i + 1) & 0xff;
			j = (j + s[i]) & 0xff;
			byte t = s[i];
			s[i] = s[j];
			s[j] = t;
			dst[doff++] = (byte) (src[soff++] ^ s[(s[i] + s[j]) & 0xff]);
		}
	}
}
