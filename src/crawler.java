import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
public class crawler implements Runnable {
    final static int AnyDepth = 0;
    private URLPool m_Pool;
    private String m_Prefix = "http";
    @Override
    public void run() {
        try {
            Scan();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    public crawler(URLPool pool) {
        m_Pool = pool;
    }

    private  void  Scan() throws IOException, InterruptedException {
        while (true) {
            Process(m_Pool.get());

        }
    }
    private void Process(URLDepthPair pair) throws IOException{
        URL url = new URL(pair.getURL());
        URLConnection connection = url.openConnection();

        String redirect = connection.getHeaderField("Location");
        if (redirect != null) {
            connection = new URL(redirect).openConnection();
        }
        m_Pool.addProcessed(pair);
        if (pair.getDepth() == 0) return;
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String input;
        while ((input = reader.readLine()) != null) {
            while (input.contains("a href=\"" + m_Prefix)) {
                input = input.substring(input.indexOf("a href=\"" + m_Prefix) + 8);
                String link = input.substring(0, input.indexOf('\"'));
                if(link.contains(" "))
                    link = link.replace(" ", "%20");
                if (m_Pool.getNotProcessed().contains(new URLDepthPair(link, AnyDepth)) ||
                        m_Pool.getProcessed().contains(new URLDepthPair(link, AnyDepth))) continue;
                m_Pool.addNotProcessed(new URLDepthPair(link, pair.getDepth() - 1));
            }
        }
        reader.close();
    }
}