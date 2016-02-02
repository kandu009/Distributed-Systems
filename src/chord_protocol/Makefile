JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
            ChordInterface.java \
            Node.java \
            FindNodeResponsePair.java \
            JoinResponse.java \
            NodeInfo.java \
            Client.java \
            ServerLogger.java \
            ClientLogger.java \
            Utils.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
