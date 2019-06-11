import com.alibaba.fastjson.JSONObject;
import org.apache.http.client.protocol.HttpClientContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *
 *
 **/
public class GetSms {

    private static HttpClientContext context = HttpClientContext.create();

    /**
     * <p>
     * 读取控制台内容
     * </p>
     */
    private static String scanner(String tip) {
        Scanner scanner = new Scanner(System.in);
        System.out.println(tip);
        while (true) {
            if (scanner.hasNext()) {
                String ipt = scanner.next();
                if (ipt != null && !ipt.equals("")) {
                    return ipt;
                }
            }
        }
    }

    private static JSONObject getTelRef(String index) throws Exception {
        String html = HttpUtil.doGet("https://shouduanxin.com/zh-cn/index_" + index, null, null, context);
        Document doc = Jsoup.parse(html);
        Elements list = doc.getElementsByClass("sms-number-list row show-grid container");
        JSONObject telNumAndRef = new JSONObject();
        for (Element element : list) {
            Elements temps = element.getElementsByTag("h3");
            if (temps.size() != 1 || temps.get(0).getElementsByTag("small").size() > 0) {
                continue;
            }
            telNumAndRef.put(temps.get(0).html(), element.getElementsByTag("a").get(0).attributes().get("href"));
        }
        return telNumAndRef;
    }

    public static void main(String[] args) throws Exception {
        System.out.print("正在获取");
        StringBuilder tips = new StringBuilder("以下是获取到的电话号码");
        JSONObject telNumAndRef = new JSONObject();
        telNumAndRef.putAll(getTelRef("1"));
        System.out.print(".");
        telNumAndRef.putAll(getTelRef("2"));
        System.out.print(".");
        telNumAndRef.putAll(getTelRef("3"));
        System.out.println(".");
        telNumAndRef.forEach((k, v) -> tips.append("\n\t").append(k));
        tips.append("\n请在以上电话号码中选择:");
        String ref;
        do {
            String input = scanner(tips.toString()).toUpperCase();
            ref = telNumAndRef.getString(input);
        } while (ref == null);
        List<String> contents = new ArrayList<>();
        while (true) {
            String html = HttpUtil.doGet("https://shouduanxin.com" + ref, null, null, context);
            Document doc = Jsoup.parse(html);
            Elements list = doc.select(".container-fluid.sms_content tbody tr");
            for (Element element : list) {
                Elements temps = element.getElementsByTag("td");
                String content = temps.get(2).html();
                if (!contents.contains(content)) {
                    contents.add(temps.get(2).html());
                    System.out.println(content);
                }
            }
            Thread.sleep(5000);
        }
    }
}
