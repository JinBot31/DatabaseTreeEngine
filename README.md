<div align="center">

![](https://img.shields.io/badge/NoSQL-Database%20Manager-89b4fa?style=for-the-badge&labelColor=1e1e2e)


<h1>Gestor de Bases de Datos No Relacional</h1>

<h4>Sistema de almacenamiento de documentos JSON con indexacion mediante arboles AVL autobalanceados</h4>

<br/>

![](https://img.shields.io/badge/Build-Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)
![](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![](https://img.shields.io/badge/JavaFX-21-3a75b0?style=for-the-badge&logo=java&logoColor=white)

</div>

## Acerca del Proyecto

> **NoSQL Database Manager** es un gestor de bases de datos no relacional desarrollado en Java, que utiliza **arboles AVL autobalanceados** como estructura de indexacion primaria.

El sistema almacena documentos en formato JSON y proporciona una interfaz grafica moderna construida con JavaFX.

## Estructura del Proyecto

<br/>

```
src/main/java/com/nosqlmanager/
│
├── App.java                     # Punto de entrada
│
├── tree/                        # Arbol AVL
│   ├── AVLTree.java            # Implementacion generica
│   └── AVLNode.java            # Nodo con altura y balance
│
├── model/                       # Modelos
│   └── JsonDocument.java       # Documento JSON
│
├── manager/                     # Logica de negocio
│   └── DatabaseManager.java    # Gestor principal
│
├── repository/                  # Acceso a datos
│   └── DocumentRepository.java # Interfaz
│
├── storage/                     # Persistencia
│   └── JsonFileStorage.java    # Almacenamiento
│
└── gui/                         # Interfaz grafica
    ├── MainView.java           # Vista principal
    └── TreeVisualizer.java     # Visualizador
```

---

## Instalacion

### Requisitos Previos

<div align="center">

| | Requisito | Version | Descarga |
|:---:|:----------|:-------:|:--------:|
| <img src="https://img.shields.io/badge/-Java-ED8B00?style=flat-square&logo=openjdk&logoColor=white"/> | Java JDK | `21+` | [oracle.com](https://www.oracle.com/java/technologies/downloads/) |
| <img src="https://img.shields.io/badge/-Maven-C71A36?style=flat-square&logo=apache-maven&logoColor=white"/> | Apache Maven | `3.8+` | [maven.apache.org](https://maven.apache.org/download.cgi) |
| <img src="https://img.shields.io/badge/-Git-F05032?style=flat-square&logo=git&logoColor=white"/> | Git | `2.0+` | [git-scm.com](https://git-scm.com/downloads) |

</div>

<br/>

### Pasos de Instalacion

**1. Clonar el repositorio**

```bash
git clone https://github.com/JkVely/BDnoSQL.git
cd BDnoSQL
```

**2. Verificar requisitos**

```bash
java --version    # Debe mostrar version 21 o superior
mvn --version     # Debe mostrar version 3.8 o superior
```

**3. Compilar el proyecto**

```bash
mvn clean compile
```

**4. Ejecutar las pruebas**

```bash
mvn test
```

---

## Uso

### Ejecutar la Aplicacion

```bash
mvn javafx:run
```

### Interfaz Grafica

![Interfaz Grafica](docs/img/screenshot.png)

### Controles de Navegacion

<div align="center">

| Accion | Control | Descripcion |
|:------:|:-------:|:------------|
| Zoom | `Ctrl` + `Scroll` | Acercar o alejar la vista del arbol |
| Mover | `Click Derecho` + `Arrastrar` | Desplazar el canvas |
| Seleccionar | `Click Izquierdo` | Ver detalles del nodo |
| Reset | Boton `⟲` | Restablecer zoom y posicion |

</div>

---

## Estructura JSON

<center>
<table>
<tr>
<td width="50%">

### Documento Individual

```json
{
    "nombre": "Juan",
    "edad": 25,
    "activo": true
}
```

</td>
<td width="50%">

### Base de Datos

```json
[ {
  "id" : 2,
  "data" : {
    "nombre" : "gio",
    "codigo" : 180
  }
},
...
]
```

</td>
</tr>
</table>
</center>

---

## Pruebas

```bash
mvn test                              # Ejecutar pruebas
mvn test -Dsurefire.useFile=false     # Con reporte detallado
```

<div align="center">

| Componente | Tests | Estado |
|:-----------|:-----:|:------:|
| AVLTree - Insercion | 5 | ![Passed](https://img.shields.io/badge/-Passed-a6e3a1?style=flat-square) |
| AVLTree - Eliminacion | 4 | ![Passed](https://img.shields.io/badge/-Passed-a6e3a1?style=flat-square) |
| AVLTree - Balanceo | 3 | ![Passed](https://img.shields.io/badge/-Passed-a6e3a1?style=flat-square) |
| DatabaseManager - CRUD | 4 | ![Passed](https://img.shields.io/badge/-Passed-a6e3a1?style=flat-square) |
| DatabaseManager - Busqueda | 3 | ![Passed](https://img.shields.io/badge/-Passed-a6e3a1?style=flat-square) |

</div>

---

## Stack Tecnologico

<div align="center">

<img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white"/>
<img src="https://img.shields.io/badge/JavaFX-3a75b0?style=for-the-badge&logo=java&logoColor=white"/>
<img src="https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white"/>
<img src="https://img.shields.io/badge/Lombok-BC4521?style=for-the-badge&logo=lombok&logoColor=white"/>
<img src="https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white"/>
<img src="https://img.shields.io/badge/VS%20Code-007ACC?style=for-the-badge&logo=visual-studio-code&logoColor=white"/>

</div>

---

## Autores

<div align="center">

| | Nombre | codigo |
|:---:|:---:|:---:|
| ![Nicole Daza badge](https://img.shields.io/badge/-ND-89b4fa?style=for-the-badge&logoColor=white) | **Nicole Daza** | |
| [![Juan Carlos Quintero badge](https://img.shields.io/badge/-JQ-a6e3a1?style=for-the-badge&logoColor=white)](https://www.github.com/JkVely) | **Juan Carlos Quintero** | `20232020172` |
| ![Juan David Avila badge](https://img.shields.io/badge/-JA-f9e2af?style=for-the-badge&logoColor=white) | **Juan David Avila** | |
| ![Brayan Castro badge](https://img.shields.io/badge/-BC-f38ba8?style=for-the-badge&logoColor=white) | **Brayan Castro** | |

</div>

---
