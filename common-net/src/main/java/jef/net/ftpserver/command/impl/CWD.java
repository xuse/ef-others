/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package jef.net.ftpserver.command.impl;

import java.io.IOException;

import jef.net.ftpserver.command.AbstractCommand;
import jef.net.ftpserver.ftplet.FileSystemView;
import jef.net.ftpserver.ftplet.FtpException;
import jef.net.ftpserver.ftplet.FtpReply;
import jef.net.ftpserver.ftplet.FtpRequest;
import jef.net.ftpserver.impl.FtpIoSession;
import jef.net.ftpserver.impl.FtpServerContext;
import jef.net.ftpserver.impl.LocalizedFtpReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <strong>Internal class, do not use directly.</strong>
 * 
 * <code>CWD  &lt;SP&gt; &lt;pathname&gt; &lt;CRLF&gt;</code><br>
 * 
 * This command allows the user to work with a different directory for file
 * storage or retrieval without altering his login or accounting information.
 * Transfer parameters are similarly unchanged. The argument is a pathname
 * specifying a directory.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a> 
 */
public class CWD extends AbstractCommand {

    private final Logger LOG = LoggerFactory.getLogger(CWD.class);

    /**
     * Execute command
     */
    public void execute(final FtpIoSession session,
            final FtpServerContext context, final FtpRequest request)
            throws IOException, FtpException {

        // reset state variables
        session.resetState();

        // get new directory name
        String dirName = "/";
        if (request.hasArgument()) {
            dirName = request.getArgument();
        }

        // change directory
        FileSystemView fsview = session.getFileSystemView();
        boolean success = false;
        try {
            success = fsview.changeWorkingDirectory(dirName);
        } catch (Exception ex) {
            LOG.debug("Failed to change directory in file system", ex);
        }
        if (success) {
            dirName = fsview.getWorkingDirectory().getAbsolutePath();
            session.write(LocalizedFtpReply.translate(session, request, context,
                    FtpReply.REPLY_250_REQUESTED_FILE_ACTION_OKAY, "CWD",
                    dirName));
        } else {
            session
                    .write(LocalizedFtpReply.translate(session, request, context,
                            FtpReply.REPLY_550_REQUESTED_ACTION_NOT_TAKEN,
                            "CWD", null));
        }
    }
}
