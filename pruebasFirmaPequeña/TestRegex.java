import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class TestRegex {
    public static void main(String[] args) {
        String xmlContent = "<test><firmaPeq>iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==</firmaPeq></test>";
        String tagName = "firmaPeq";
        
        String pattern = "<" + Pattern.quote(tagName) + ">([^<]+)</" + Pattern.quote(tagName) + ">";
        System.out.println("Patrón: " + pattern);
        
        Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(xmlContent);
        
        if (m.find()) {
            String result = m.group(1);
            System.out.println("¡ÉXITO! Encontrado: " + result);
            System.out.println("Longitud: " + result.length());
        } else {
            System.out.println("No encontrado");
        }
    }
}
