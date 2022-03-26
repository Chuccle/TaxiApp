package com.example.taxiapp


data class DistanceMatrixRoot(

    val destination_addresses: List<String>,

    val origin_addresses: List<String>,

    val rows: List<Row>,

    val status: String,

    )

data class Row(

    val elements: List<Element>,

    )

data class Element(

    val distance: Distance,

    val duration: Duration,

    val status: String,

    )

data class Distance(

    val text: String,

    val value: Int,

    )

data class Duration(

    val text: String,

    val value: Int,

    )

/*

 useful row 0 element 0    currentlocation to pickup distance
 useful row 0 element 0    currentlocation to pickup duration

 useful row 1 element 1  pickup to destination distance
 useful row 1 element 1  pickup to destination duration

 useless row 1 element 0  currentlocation to destination distance
 useless row 1 element 0  currentlocation to pickup duration

 useful row 0 element 1    pickup to pickup distance
 useful row 0 element 1    pickup to pickup duration

 */