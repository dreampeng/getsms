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

    private static JSONObject getTelRef(String url, String listCssQuery, String phoneCssQuery) throws Exception {
        String html = HttpUtil.doGet(url, null, null, context);
        Document doc = Jsoup.parse(html);
        Elements list = doc.select(listCssQuery);
        JSONObject telNumAndRef = new JSONObject();
        for (Element element : list) {
            String href = element.getElementsByTag("a").get(0).attr("href");
            String phone = element.select(phoneCssQuery).get(0).text().replaceAll(" ", "");
            telNumAndRef.put(phone, href);
        }
        return telNumAndRef;
    }

    private static List<String> getNewSms(String ref, String listCssQuery, List<String> oldSMS) throws Exception {
        String html = HttpUtil.doGet(ref, null, null, context);
        List<String> newSMS = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        Elements list = doc.select(".layui-table tbody tr");
        for (Element element : list) {
            String content = element.text();
            if (!oldSMS.contains(content)) {
                newSMS.add(content);
            }
        }
        return newSMS;
    }

    public static void main(String[] args) throws Exception {
        System.out.print("正在获取");
        StringBuilder tips = new StringBuilder("以下是获取到的电话号码");
        JSONObject telNumAndRef = new JSONObject();
        String listCssQuery = ".main .layui-card .layuiadmin-card-list";
        String phoneCssQuery = "a";
        for (int i = 0; i < 3; i++) {
            System.out.print(".");
            String url = "https://www.yinsixiaohao.com/dl/" + (i + 1) + ".html";
            telNumAndRef.putAll(getTelRef(url, listCssQuery, phoneCssQuery));
        }
        telNumAndRef.forEach((k, v) -> tips.append("\n\t").append(k));
        tips.append("\n请在以上电话号码中选择:");
        String ref;
        do {
            String input = scanner(tips.toString()).toUpperCase();
            ref = telNumAndRef.getString(input);
        } while (ref == null);
        List<String> oldSMS = new ArrayList<>();
        listCssQuery = ".layui-table tbody tr td:eq(1)";
        System.out.println("正在读取短信...");
        while (true) {
            List<String> newSMS = getNewSms(ref, listCssQuery, oldSMS);
            oldSMS.addAll(newSMS);
            for (int i = newSMS.size() -1 ; i >= 0 ; i--) {
                System.out.println(newSMS.get(i));
            }
            scanner("请在发送短信后输入\"1\":");
        }
    }
}
