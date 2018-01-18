package jef.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import jef.tools.IOUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KnownHostsEx;
import com.jcraft.jsch.Session;

/**
 * This class provide interface to execute command on remote Linux.
 */

public class SSHCommandExecutor {
	private static Logger log = LoggerFactory.getLogger(SSHCommandExecutor.class);
	private String ipAddress;

	private String username;

	private String password;

	public static final int DEFAULT_SSH_PORT = 22;

	JSch jsch = new JSch();

	private Session session;

	private ReceiveThread receiver;

	public SSHCommandExecutor(final String ipAddress, final String username, final String password) {
		this.ipAddress = ipAddress;
		this.username = username;
		this.password = password;
		jsch.setHostKeyRepository(new KnownHostsEx(jsch));
	}

	public void open() {
		try {
			session = jsch.getSession(username, ipAddress, DEFAULT_SSH_PORT);
			session.setPassword(password);
			session.setUserInfo(new MyUserInfo());
			session.setTimeout(10000);
			session.connect();
			this.receiver = new ReceiveThread((ChannelShell) session.openChannel("shell"));
		} catch (JSchException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		this.receiver.close();
		if (session != null) {
			session.disconnect();
			log.info("SftpSession {} closed.",this);
			session = null;
		}
	}

	static class ReceiveThread {
		private ChannelShell channel;
		private BufferedWriter writer;
		final Queue<String> buffer = new ConcurrentLinkedQueue<String>();
		private BufferedReader input;
		private Random random=new Random();

		private ReceiveThread(ChannelShell channel) throws IOException, JSchException {
			this.channel = channel;
			this.input = new BufferedReader(new InputStreamReader(channel.getInputStream(), "UTF-8"));
			this.writer = new BufferedWriter(new OutputStreamWriter(channel.getOutputStream(), "UTF-8"));
			channel.setInputStream(null);
			channel.connect();
			//停止命令回显
			writer.write("stty -echo\n");
			writer.flush();
			this.buffer.clear();
		}

		public void close() {
			if(writer!=null) {
				try {
					writer.write("exit\n");
					writer.flush();	
				}catch(IOException e) {
					log.error("Error wile closing command sender,",e);
				}
				IOUtils.closeQuietly(writer);
				writer=null;
			}
			if (channel != null) {
				channel.disconnect();
				log.info("SSHChannel {} closed.",channel);
				channel = null;
			}
		}

		public int execute(final String command) {
			ensureOpen();
			try {
				log.debug("sending: {}",command);
				String id=String.valueOf(random.nextInt());
				writer.write(command);
				writer.write(13);
				writer.write("echo __END"+id);
				writer.write(13);
				writer.flush();
				return awaitStatus(id);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		private int awaitStatus(String id) throws IOException {
			String flag="__END"+id;
			String line;
			while ((line = input.readLine()) != null) {
				if(!line.startsWith("echo ") && line.endsWith(flag)) {
					break;
				}else {
					buffer.add(line);
				}
			}
			return channel.getExitStatus();
		}

		private void ensureOpen() {
			if (channel == null) {
				throw new IllegalStateException("Executor was not connected yet");
			}
		}
	}


	public ExecuteResult execute(String string) {
		System.out.println("================================================");
		int status = receiver.execute(string);
		List<String> output = new ArrayList<String>();
		String line;
		while ((line = receiver.buffer.poll()) != null) {
			output.add(line);
		}
		return new ExecuteResult(status, output);
	}

	public static class ExecuteResult {
		private List<String> output;

		public ExecuteResult(int status, List<String> output) {
			this.output = output;
		}

		public List<String> getOutput() {
			return output;
		}

		public void setOutput(List<String> output) {
			this.output = output;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(128);
			Iterator<String> iter = output.iterator();
			if (iter.hasNext()) {
				sb.append(iter.next());
			}
			while (iter.hasNext()) {
				sb.append("\r\n").append(iter.next());
			}
			return sb.toString();
		}
	}
	

	public static void main(final String[] args) {
		SSHCommandExecutor sshExecutor = new SSHCommandExecutor("10.33.40.163", "root", "123456");
		sshExecutor.open();
		ExecuteResult result = sshExecutor.execute("pwd");
		System.out.println(result);

		result = sshExecutor.execute("ls -al");
		System.out.println(result);

		result = sshExecutor.execute("cd aaa");
		System.out.println(result);

		result = sshExecutor.execute("pwd");
		System.out.println(result);

		sshExecutor.close();
	}
}