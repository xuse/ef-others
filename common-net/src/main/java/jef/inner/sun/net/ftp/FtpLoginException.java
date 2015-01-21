/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package jef.inner.sun.net.ftp;


/**
 * This exception is thrown when an error is encountered during an
 * FTP login operation. 
 * 
 * @version 	%I%, %G%
 * @author	Jonathan Payne
 */
public class FtpLoginException extends FtpProtocolException {
	private static final long serialVersionUID = 1L;

	FtpLoginException(String s) {
	super(s);
    }
}

