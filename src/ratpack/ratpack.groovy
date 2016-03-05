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
            render groovyTemplate("index.html", title: "NameMachine")
        }

        get("api/names/:gender") {
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
