package spring.tests;

import com.singingbush.sdl.spring.SdlPropertySourceFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes= SpringAppExampleTest.SdlPropertiesConfiguration.class,
    loader=AnnotationConfigContextLoader.class
)
public class SpringAppExampleTest {

    @Configuration
    @PropertySource(value = "classpath:my-test.sdl", factory = SdlPropertySourceFactory.class)
    public static class SdlPropertiesConfiguration {}

    @Value("${title}")
    private String titleName;

    @Value("${bookmarks}")
    private int[] bookmarks;

    // author "Peter Parker" email="peter@example.org" active=true
    @Value("${author}")
    private String authorName;

    //@Value("${author[email]}")
    @Value("${author|email}")
    private String authorEmail;

    //@Value("${author[active]}")
    @Value("${author|active}")
    private Boolean authorActive;

    @Value("${author|nonexistentString:#{null}}")
    private String authorNonexistentString;

    @Value("${author|nonexistentBool:true}")
    private Boolean authorNonexistentBool;


    @Value("${contents.section.paragraph[0]}") // should be "This is the first paragraph"
    private String paragraph0;

    @Value("${contents.section.paragraph[1]}") // should be "This is the second paragraph"
    private String paragraph1;

    @DisplayName("Basic SDL tag: 'title \"Hello, World\"'")
    @Test
    public void testSdlTagValueByName_basic() {
        assertNotNull(titleName);
        assertEquals("Hello, World", titleName);
    }

    @DisplayName("Basic SDL tag: 'bookmarks 12 15 188 1234'")
    @Test
    public void testSdlTagValueByName_Array() {
        assertNotNull(bookmarks);
        assertArrayEquals(new int[] {12, 15, 188, 1234}, bookmarks);
    }

    @DisplayName("Make sure a SDL node can be found by name and the value found (String)")
    @Test
    public void testSdlTagValueByName() {
        assertNotNull(authorName);
        assertNotEquals("${author}", authorName);
        assertEquals("Peter Parker", authorName);
    }

    @DisplayName("Make sure a SDL node can be found by name and a named String attribute found")
    @Test
    public void testSdlTagAttributeValueByName_String() {
        assertNotNull(authorEmail);
        assertNotEquals("${author[email]}", authorEmail);
        assertEquals("peter@example.org", authorEmail);
    }

    @DisplayName("Make sure a SDL node can be found by name and a named boolean attribute found")
    @Test
    public void testSdlTagAttributeValueByName_Bool() {
        assertNotNull(authorActive);
        assertTrue(authorActive);
    }

    @DisplayName("Handle attempt to find invalid attribute on SDL node with a Spring EL null default value (should be null)")
    @Test
    public void testSdlTagAttributeWithoutDefaultAndDoesntExist() {
        assertNull(authorNonexistentString);
    }

    @DisplayName("Handle attempt to find invalid attribute on SDL node with a default value (should use default)")
    @Test
    public void testSdlTagAttributeDefaultValueIfDoesntExist() {
        assertNotNull(authorNonexistentBool);
        assertTrue(authorNonexistentBool);
    }

    @DisplayName("Should be able to find nested nodes and select by index if multiple nodes in same hierarchy have same name")
    @Test
    public void testNestedSdlNodes() {
        assertEquals("This is the first paragraph", paragraph0);
        assertEquals("This is the second paragraph", paragraph1);
    }

}
