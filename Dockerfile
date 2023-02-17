########################################################################################
# Base image stub for pyNLDI-Crawler
# Initial template from code.usgs.gov/wma/pygeoapi and Clifford Hill

FROM python:3.10-slim as root-cert
#FROM ubuntu:22.04 as root-cert 

USER root

LABEL maintainer1="Gene Trantham <gtrantham@contractor.usgs.gov>"
LABEL maintainer2="Erik Wojtylko <ewojtylko@usgs.gov>"

# Needs DOI Cert to work.  VPN is needed for artifactory image and cert is needed to pip install through vpn.
COPY ./docker/DOIRootCA2.cer /usr/local/share/ca-certificates/DOIRootCA2.crt
RUN chmod 644 /usr/local/share/ca-certificates/DOIRootCA2.crt
RUN update-ca-certificates

ENV PIP_CERT="/etc/ssl/certs/ca-certificates.crt" \
    SSL_CERT_FILE="/etc/ssl/certs/ca-certificates.crt" \
    CURL_CA_BUNDLE="/etc/ssl/certs/ca-certificates.crt" \
    REQUESTS_CA_BUNDLE="/etc/ssl/certs/ca-certificates.crt"

########################################################################################
# Set up Ubuntu Linux Environment now that certs are in order
FROM root-cert as ubuntu-base

RUN mkdir -p /nldi-crawler-py
WORKDIR /nldi-crawler-py
RUN pip install -U pip setuptools
RUN pip install poetry
COPY . .
RUN poetry install
#RUN poetry add psycopg-binary
# The rest...  also need to clean up chaff
