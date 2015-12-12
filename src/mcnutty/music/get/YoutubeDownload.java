package mcnutty.music.get;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.UUID;

public class YoutubeDownload implements Runnable {

	String URL;
	ProcessQueue process_queue;
	String ip;
	String directory;
	
	public YoutubeDownload(String URL, ProcessQueue process_queue, String ip, String directory) {
		this.URL = URL;
		this.process_queue = process_queue;
		this.ip = ip;
		this.directory = directory;
	}
	
	@Override
	public void run() {
		if (URL.contains("youtube.com") && URL.contains("&list")) {
			URL = URL.substring(0, URL.indexOf("&list"));
		}
		
		
		System.out.println("Starting download of video " + URL + " queued by " + ip);
		QueueItem temp = new QueueItem("null", URL, ip);
		process_queue.bucket_youtube.add(temp);
		String guid = UUID.randomUUID().toString();
		ProcessBuilder pb = new ProcessBuilder(
                Paths.get("").toAbsolutePath().toString() + "/youtube-dl", "--get-filename"
                , "-o%(title)s", "--restrict-filenames", "--no-playlist", URL);
        Process p;
		try {
			p = pb.start();
	        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
	        StringBuilder sb = new StringBuilder();
	        String line;
        	while ((line = br.readLine()) != null) {
			    sb.append(line);
			}
        	ProcessBuilder downloader = new ProcessBuilder(Paths.get("").toAbsolutePath().toString() + "/youtube-dl"
        			, "-o", System.getProperty("java.io.tmpdir") + directory + guid, "--restrict-filenames", "-f mp4",URL);
        	Process dl;
			dl = downloader.start();
			dl.waitFor();
			
			String extension = "";
			String real_name = sb.toString();
			process_queue.new_item(new QueueItem(guid + extension, real_name, ip));
			System.out.println("Downloaded file " + real_name + " for " + ip);
			process_queue.bucket_youtube.remove(temp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}