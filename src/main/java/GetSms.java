import com.alibaba.fastjson.JSONObject;
import org.apache.http.client.protocol.HttpClientContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *
 *
 **/
public class GetSms {

    private JSONObject phoneList;
    private List<String> msgList;

    private static HttpClientContext context = HttpClientContext.create();

    /**
     * <p>
     * 读取控制台内容
     * </p>
     */
    private static String scanner() {
        Scanner scanner = new Scanner(System.in);
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
        JSONObject phoneInfo = new JSONObject();
        for (Element element : list) {
            JSONObject phoneAndRef = new JSONObject();
            String href = element.getElementsByTag("a").get(0).attr("href");
            String country = element.getElementsByTag("img").get(0).attr("alt");
            String phone = element.select(phoneCssQuery).get(0).text().replaceAll(" ", "");
            phoneAndRef.put("href", href);
            phoneAndRef.put("country", country);
            phoneInfo.put(phone, phoneAndRef);
        }
        return phoneInfo;
    }

    private static List<String> getNewSms(String ref, String listCssQuery, List<String> oldSMS) throws Exception {
        String html = HttpUtil.doGet(ref, null, null, context);
        List<String> newSMS = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        Elements list = doc.select(listCssQuery);
        for (Element element : list) {
            String content = element.text();
            if ("超级云短信".equals(content)) {
                continue;
            }
            if (!oldSMS.contains(content)) {
                newSMS.add(content);
            }
        }
        return newSMS;
    }

    private void initPhoneInfo() throws Exception {
        System.out.print("正在获取");
        this.phoneList = new JSONObject();
        String listCssQuery = ".phone_item";
        String phoneCssQuery = "a";
        for (int i = 0; i < 3; i++) {
            System.out.print(".");
            String url = "http://www.bfkdim.com/?page=" + i;
            try {
                this.phoneList.putAll(getTelRef(url, listCssQuery, phoneCssQuery));
            } catch (UnknownHostException uhe) {
                System.out.println("\n你好象已经被网站屏蔽了，请明日再试\nSee You Tomorrow\nOR\nUse Secret Method");
                Thread.sleep(1000 * 3);
                System.exit(1);
            }
        }
        StringBuilder tips = new StringBuilder("以下是获取到的电话号码");
        this.phoneList.forEach((k, v) -> tips.append("\n\t").append(k).append("(").append(((JSONObject) v).getString("country")).append(")"));
        tips.append("\n输入“RE”重新初始化，“1”刷新短信，“2”结束").append("\n请在以上电话号码中选择:");
        System.out.println(tips.toString());
    }

    private void getMsgInfo(String ref) throws Exception {
        System.out.println("正在读取短信...");
        List<String> newSMS = getNewSms("http://www.bfkdim.com" + ref, ".panel-footer a", this.msgList);
        this.msgList.addAll(newSMS);
        for (int i = newSMS.size() - 1; i >= 0; i--) {
            System.out.println(newSMS.get(i));
        }
    }


    public static void main(String[] args) throws Exception {
        GetSms getSms = new GetSms();
        getSms.initPhoneInfo();
        String ref = null;
        getSms.msgList = new ArrayList<>();
        do {
            String input = scanner().toUpperCase();
            if ("1".equals(input)) {
                if (ref == null) {
                    System.out.println("请先选择电话号码：");
                } else {
                    getSms.msgList = new ArrayList<>();
                    getSms.getMsgInfo(ref);
                }
            } else if ("2".equals(input)) {
                System.out.println("See You Next Time !");
                Thread.sleep(1000 * 3);
                break;
            } else if ("RE".equals(input)) {
                System.out.println("\n\n\n\n\n\n\n\n重新获取\n\n\n\n\n\n\n");
                getSms.initPhoneInfo();
            } else {
                try {
                    ref = getSms.phoneList.getJSONObject(input).getString("href");
                    getSms.getMsgInfo(ref);
                } catch (NullPointerException e) {
                    System.out.println("不存在该号码，请重新输入");
                }
            }
        } while (true);
    }
}
