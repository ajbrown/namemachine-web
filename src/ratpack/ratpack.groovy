import groovy.time.TimeCategory
import org.ajbrown.namemachine.Gender
import org.ajbrown.namemachine.NameGenerator
import org.ajbrown.namemachine.NameGeneratorOptions
import ratpack.groovy.template.TextTemplateModule

import static ratpack.groovy.Groovy.ratpack
import static ratpack.jackson.Jackson.json
import static ratpack.groovy.Groovy.groovyTemplate

ratpack {
    bindings {
        module TextTemplateModule, { TextTemplateModule.Config config -> config.staticallyCompile = true }
    }

    handlers {

        get {

            def expires = new Date()
            use(TimeCategory) {
                expires = expires + 1.hour
            }
            response.headers.add( "Cache-Control", "max-age=3600, must-revalidate" )
            response.headers.add( "Expires", expires.toGMTString() )

            render groovyTemplate("index.html", title: "NameMachine")
        }

        get("api/names/:gender") {

            response.headers.add( "Cache-control", "private, max-age=0, no-cache" )
            response.headers.add( "Expires", "Thu, 01-Jan-2016 18:45:20 GMT")

            def count  = request.queryParams.getOrDefault( 'count', "100" ) //TODO no more than 1000 allowed at a time
            def weight = request.queryParams.getOrDefault( 'genderWeight', "${NameGeneratorOptions.DEFAULT_GENDER_WEIGHT}" )
            def gender = Gender.values().find{ it.toString() == pathTokens['gender']?.toUpperCase() }
            def options = new NameGeneratorOptions( genderWeight: weight.toDouble() )

            def names = new NameGenerator( options ).generateNames( count.toInteger(), gender )
            //TODO should we stream these?
            render( json( names ) )
        }

        fileSystem "public", { f -> f.files() }
    }
}
