FROM eclipse-temurin:21-jdk-jammy

# Versions
ENV GATLING_VERSION=3.10.0
ENV GATLING_HOME=/opt/gatling

# Install tools, X11 libs (required for Gatling Recorder Swing GUI) and download Gatling
RUN apt-get update && apt-get install -y curl unzip \
    libxext6 libxi6 libxrender1 libxtst6 libxrandr2 fonts-dejavu-core \
    && rm -rf /var/lib/apt/lists/* \
    && mkdir -p /opt \
    && curl -L -o /tmp/gatling-bundle.zip \
       https://repo1.maven.org/maven2/io/gatling/highcharts/gatling-charts-highcharts-bundle/${GATLING_VERSION}/gatling-charts-highcharts-bundle-${GATLING_VERSION}-bundle.zip \
    && unzip /tmp/gatling-bundle.zip -d /opt \
    && mv /opt/gatling-charts-highcharts-bundle-* ${GATLING_HOME} \
    && rm /tmp/gatling-bundle.zip

# Créer les répertoires nécessaires pour Gatling
RUN mkdir -p ${GATLING_HOME}/user-files/simulations \
    && mkdir -p ${GATLING_HOME}/user-files/bodies \
    && mkdir -p ${GATLING_HOME}/user-files/data \
    && mkdir -p ${GATLING_HOME}/results

# Supprimer les simulations d'exemple pour éviter les conflits
RUN rm -rf ${GATLING_HOME}/user-files/simulations/*

# Copier les simulations dans le répertoire Gatling
COPY simulations ${GATLING_HOME}/user-files/simulations

# Copier le script d'entrée
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# Classe de simulation à lancer (sans package)
ENV GATLING_SIMULATION_CLASS=BasicSimulation

WORKDIR ${GATLING_HOME}

# Utiliser le script d'entrée
ENTRYPOINT ["/entrypoint.sh"]