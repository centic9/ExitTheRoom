[![Build Status](https://github.com/centic9/ExitTheRoom/actions/workflows/gradle-build.yml/badge.svg)](https://github.com/centic9/ExitTheRoom/actions)
[![Gradle Status](https://gradleupdate.appspot.com/centic9/ExitTheRoom/status.svg?branch=master)](https://gradleupdate.appspot.com/centic9/ExitTheRoom/status)
[![Release](https://img.shields.io/github/release/centic9/ExitTheRoom.svg)](https://github.com/centic9/ExitTheRoom/releases)
[![GitHub release](https://img.shields.io/github/release/centic9/ExitTheRoom.svg?label=changelog)](https://github.com/centic9/ExitTheRoom/releases/latest)
[![Tag](https://img.shields.io/github/tag/centic9/ExitTheRoom.svg)](https://github.com/centic9/ExitTheRoom/tags)

## Overview

A small project which provides sources and instructions (in German) for
a simple Exit-The-Room game geared towards children.

It is written in Java and uses a Raspberry Pi and a TM1638 display/keypad-device to 
simulate a time-bomb where time is running out if the players do not manage to 
defuse it quickly enough.

Included:
* Java sources using Pi4j
  * `ButtonDigitSpinning` - main application
* `Anleitung.docx` - instructions and setup-information in German 
* Some Python-Samples for testing

Used hardware:
* Raspberry Pi
* A TM1638 combined keypad and display device
* A simple audio-buzzer for making strange noises

Pre-requisites:
* Raspberry Pi with Linux OS
  * omxplayer available

## Getting started

### Grab it

    git clone https://github.com/centic9/ExitTheRoom.git

### Build it and create the distribution files

    cd ExitTheRoom
    ./gradlew check

### Run it

    ./gradlew buttonDigitSpinning

## Support this project

If you find this library useful and would like to support it, you can [Sponsor the author](https://github.com/sponsors/centic9)

## Licensing

* ExitTheRoom is licensed under the MIT License, see file `LICENSE`
