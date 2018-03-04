package DistributedSchnorr;

class SetupVariables {
    final static String systemName = "helloWorldRemoteAS";
    final static String ip = "xxx.xx.xx.x"; //TODO
    final static String port = "2600";
    final static String serverActorName = "ServerMain";
    final static String InitialHostPath = "akka.tcp://" + systemName + "+@"+ip+":"+port+"/user/"+ serverActorName;
    final static int peerCount = 3;

}
