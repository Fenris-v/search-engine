import parser.Parser;

public class Main {
//    private static final Connection CONNECTION = new Connection();

    private static final Parser parser = new Parser();

    public static void main(String[] args) {
        parser.parseSite();

//        try (Session session = CONNECTION.getSession()) {
//            Page page = new Page();
//            page.setCode(200);
//            page.setContent("djfhsdkfhskdfsdkjfhskdhfksdfkjsdhfkshdfkhsdkfhksdfhkfhdskjfhdskjfhskjdhf");
//            page.setPath("/asd");
//            session.saveOrUpdate(page);
//        }
    }
}
