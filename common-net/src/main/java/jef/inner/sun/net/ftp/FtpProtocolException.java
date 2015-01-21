/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package jef.inner.sun.net.ftp;

import java.io.*;

/**
 * This exeception is thrown when unexpected results are returned during
 * an FTP session.
 *
 * @version	%I%, %G%
 * @author	Jonathan Payne
 */
public class FtpProtocolException extends IOException {
	private static final long serialVersionUID = 1L;

	FtpProtocolException(String s) {
	super(s);
    }
}

