module sdlang {
    requires static org.jetbrains.annotations; // for the annotation pre-processing to work (static will make it compile time only)

    exports com.singingbush.sdl;
}
