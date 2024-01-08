package com.singingbush.sdl.annotations;

import com.singingbush.sdl.SDL;
import com.singingbush.sdl.SDLParseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AnnotationTest {

    @DisplayName("Ensure annotated pojo Serialization & Deserialization")
    @Test
    public void testAnnotatedPojoSerializationDeserialization() throws IOException, SDLParseException {
        final Person p = new Person("bob", "bob@website.test", LocalDate.of(2000, 1, 28));

        final String sdl = SDL.toSDL(p);

        assertEquals("person \"bob\" dob=2000/1/28 email=\"bob@website.test\"", sdl, "Annotated pojo should serialize correctly");

        final Optional<Person> op = SDL.fromSDL(sdl, Person.class);

        assertTrue(op.isPresent());

        final Person person = op.get();
        assertEquals("bob", person.getUsername());
        assertEquals("bob@website.test", person.getEmail());
        assertEquals(LocalDate.of(2000, 1, 28), person.getDob());

        assertEquals(p, person);
    }

    @DisplayName("Ensure deserialization ignores unrelated tags")
    @Test
    public void test() throws SDLParseException, IOException {
        final Optional<Person> op = SDL.fromSDL(
            "car \"Tesla\"\n" +
            "person \"Dave\" dob=1995/5/23 email=\"dave@website.test\"\n" +
            "house type=\"detached\"",
            Person.class
        );

        assertTrue(op.isPresent());

        final Person person = op.get();
        assertEquals("Dave", person.getUsername());
        assertEquals("dave@website.test", person.getEmail());
        assertEquals(LocalDate.of(1995, 5, 23), person.getDob());
    }

    /*
    * This class is an example of how to annotate a pojo for serialization/derserialization to SDLang
    */
    @Tag("person")
    public static class Person {

        @Value
        private String username;

        @Attribute
        private String email;

        @Attribute("dob")
        private LocalDate dob;

        public Person() {} // SDL annotations require nullary constructor at the minute

        public Person(String username, String email, LocalDate dob) {
            this.username = username;
            this.email = email;
            this.dob = dob;
        }

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }

        public LocalDate getDob() {
            return dob;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Person person = (Person) o;
            return Objects.equals(username, person.username) && Objects.equals(email, person.email) && Objects.equals(dob, person.dob);
        }

        @Override
        public int hashCode() {
            return Objects.hash(username, email, dob);
        }
    }
}
