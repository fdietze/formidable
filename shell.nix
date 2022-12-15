{ pkgs ? import <nixpkgs> {}}:
let
  pname = "formidable";
  cypress11  = import (builtins.fetchTarball https://github.com/r-ryantm/nixpkgs/archive/refs/heads/auto-update/cypress.tar.gz) {};
in
  pkgs.mkShell {
    nativeBuildInputs = with pkgs; [
      git

      yarn
      nodejs-18_x

      sbt
    ];

    buildInputs = with pkgs; [
      cypress11.cypress
    ];

    installPhase= ''
    '';

    TMPDIR = "/tmp";

    shellHook = with pkgs; ''
      echo --- Welcome to ${pname}! ---

      export JAVA_HOME="$('grep' -e '^-java-home' ${pkgs.sbt}/share/sbt/conf/sbtopts | cut -d ' ' -f 2)"

      export CYPRESS_INSTALL_BINARY=0
      export CYPRESS_RUN_BINARY=${pkgs.cypress}/bin/Cypress

      echo "sbt jdk path = $JAVA_HOME"
    '';
  }
