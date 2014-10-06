#
# A simple makefile for compiling three java classes
#

# define a makefile variable for the java compiler
#
JCC = javac

SOURCES= \
CrawlerMain.java

# define a makefile variable for compilation flags
# the -g flag compiles with debugging information
#
JFLAGS = -g

# typing 'make' will invoke the first target entry in the makefile 
# (the default one in this case)
#
default: CrawlerMain.class

CrawlerMain.class: CrawlerMain.java
	$(JCC) $(JFLAGS) -cp ./lib/jsoup-1.7.3.jar CrawlerMain.java
	
clean: 
	$(RM) *.class