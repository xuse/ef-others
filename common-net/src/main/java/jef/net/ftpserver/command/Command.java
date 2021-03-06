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

package jef.net.ftpserver.command;

import java.io.IOException;

import jef.net.ftpserver.ftplet.FtpException;
import jef.net.ftpserver.ftplet.FtpRequest;
import jef.net.ftpserver.impl.FtpIoSession;
import jef.net.ftpserver.impl.FtpServerContext;

/**
 * This interface encapsulates all the FTP commands.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a> 
 */
public interface Command {

    /**
     * Execute command.
     * 
     * @param session
     *            The current {@link FtpIoSession}
     * @param context
     *            The current {@link FtpServerContext}
     * @param request The current {@link FtpRequest}
     * @throws IOException 
     * @throws FtpException 
     */
    void execute(FtpIoSession session, FtpServerContext context,
            FtpRequest request) throws IOException, FtpException;

}
