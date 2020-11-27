package de.mbrauner.unifibackup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class SSH {

  private UnifiProperties p;

  public void perform() throws IOException, JSchException {
    JSch jsch = new JSch();
    String host = p.getProperty(UnifiProperties.HOST);
    int port = UnifiProperties.PORT_DEFAULT;

    if (p.getProperty(UnifiProperties.PORT, "").matches("\\d+")) {
      port = Integer.parseInt(p.getProperty(UnifiProperties.PORT));
    }

    Session session = jsch.getSession(p.getProperty(UnifiProperties.USER), host, port);
    session.setPassword(p.getProperty(UnifiProperties.PW));
    session.setConfig("StrictHostKeyChecking", "no");
    session.connect();

    List<String> fileNames = listFiles(session);
    getFiles(session, fileNames);

    session.disconnect();
  }

  protected void getFiles(Session session, List<String> fileNames) throws IOException, JSchException {
    Channel channel = session.openChannel("exec");
    for (String fileName : fileNames) {
      LOGGER.debug("try to fetch  {}", fileName);
      ChannelExec.class.cast(channel).setCommand("scp -f " + fileName);
      InputStream commandOutput = channel.getInputStream();
      channel.connect();
      byte[] arr = new byte[commandOutput.available()];
      int stillAvailable = commandOutput.read(arr);
      if (stillAvailable > 0) {
        LOGGER.warn("possible error detected, still {} bytes to read", stillAvailable);
      }
      channel.disconnect();
      final String file = getRightPath(p.getProperty(UnifiProperties.LOCALPATH, UnifiProperties.LOCALPATH_DEFAULT))
        + fileName.substring(fileName.lastIndexOf(File.separator) + 1);
      try (FileOutputStream fos = new FileOutputStream(file);) {
        fos.write(arr);
        fos.flush();
      }
      LOGGER.info("downloaded {}", fileName);
    }
  }

  private String getRightPath(@NonNull String path) {
    if (!path.endsWith(File.separator)) {
      return path + File.separator;
    } else {
      return path;
    }
  }

  protected List<String> listFiles(Session session) throws IOException, JSchException {
    Channel channel = session.openChannel("exec");
    ChannelExec.class.cast(channel).setCommand("ls " + p.getProperty(UnifiProperties.REMOTE, UnifiProperties.REMOTE_DEFAULT));
    InputStream commandOutput = channel.getInputStream();
    channel.connect();
    List<String> list = IOUtils.readLines(commandOutput, Charset.defaultCharset());
    channel.disconnect();
    LOGGER.debug("listing return {}", list);
    return list;
  }
}
