# lein-datomic

This is a very basic Leiningen plugin to start a Datomic transactor and (re-)initialize the database with a schema based on project settings.

## Usage

Put `[lein-datomic "0.2.0"]` into the `:plugins` vector of your
`:user` profile, or if you are on Leiningen 1.x do `lein plugin install
lein-datomic 0.2.0`.

You will need to download a zip from
[Datomic](http://www.datomic.com/get-datomic.html) 
and extract it.  This extraction location is your :install-location.

Next you'll need to configure your :user profile in ~/.lein/profiles.clj to add a :datomic
key to a map with an :install-location like so:

    {:user
     {:plugins [[lein-datomic "0.2.0"]]
      :datomic {:install-location "/path/to/your/install/dir/datomic-free-0.8.3619"}}}

In your project, add a configuration like:

    (defproject ...
      :datomic {:schemas ["resources/schema" ["my-schema.edn"
                                              "initial-data.edn"]]}
      :profiles {:dev
                 {:datomic {:config "resources/free-transactor-template.properties"
                            :db-uri "datomic:free://localhost:4334/my-db"}}})

Now you're ready to start a transactor and load up your schemas and initial data:

    $ lein datomic start &
    $ lein datomic initialize   #this requires a running transactor

## License

Copyright © 2012 Wayne Rittimann, Jr.

Distributed under the Eclipse Public License, the same as Clojure.
