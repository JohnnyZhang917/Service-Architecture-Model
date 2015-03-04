resolvers += Classpaths.typesafeReleases

addSbtPlugin("de.johoop" % "sbt-testng-plugin" % "3.0.2")

resolvers += "iDecide 3rd party Releases" at "https://nexus.flexis.ru/content/repositories/thirdparty"

addSbtPlugin("com.github.sbt" %% "sbt-scalabuff" % "0.4.0_1.4.0")
