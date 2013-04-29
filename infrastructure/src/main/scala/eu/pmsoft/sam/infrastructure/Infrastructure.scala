package eu.pmsoft.sam.infrastructure

//import com.twitter.finagle.{Server, Service}
//import com.twitter.util.Future
//import java.net.InetSocketAddress
//import com.twitter.finagle.builder.{ServerBuilder, Server}
//import com.twitter.finagle.builder.Server
//import eu.pmsoft.sam.architecture.definition.SamArchitectureDefinition
//import eu.pmsoft.sam.definition.implementation.SamServiceImplementationPackageContract
//import eu.pmsoft.sam.architecture.loader.ArchitectureModelLoader
//;


//object Infrastructure {
//
//
//
//  val canonicalProtocolService = new Service[String,String] {
//    def apply(request: String) = Future.value(request)
//  }
//
//  def main(args: Array[String]) {
//    val service = new Service[String, String] {
//      def apply(request: String) = Future.value(request + " from server")
//    }
//
//    // Bind the service to port 8080
//    val server: Server = ServerBuilder()
//      .codec(ObjectSerializationCodec)
//      .bindTo(new InetSocketAddress(8080))
//      .name("echoserver")
//      .build(service)
//  }
//}
//
//class Infrastructure(configuration : ExecutionNodeConfiguration) {
//
//  val localNodeServer = configuration.finagleServer
//
//  val architectures = configuration.architectureDefinitions map ArchitectureModelLoader.loadArchitectureModel _
//
//}
//
//case class InfrastructureInitialiationError(message : String, exception : Option[Exception])
//
//sealed case class ExecutionNodeConfiguration(
//       finagleServer : Option[Server] = None,
//       architectureDefinitions : Seq[SamArchitectureDefinition] ,
//       implementationPackages : Seq[SamServiceImplementationPackageContract]
//                                                                     ) {}