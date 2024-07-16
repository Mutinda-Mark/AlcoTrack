<?php
// Database credentials
$servername = "192.168.158.182";
$username = "root"; 
$password = ""; 
$dbname = "project-1"; 

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// Prepare SQL query to fetch highest alcohol readings for each day
$sql = "SELECT DATE(V_Timestamp) AS Date, MAX(V_AlcoholLevel) AS HighestAlcohol
        FROM tbl_values
        GROUP BY DATE(V_Timestamp)
        ORDER BY DATE(V_Timestamp) ASC";

$result = $conn->query($sql);

$readings = array();
if ($result->num_rows > 0) {
    // Output data of each row
    while ($row = $result->fetch_assoc()) {
        $readings[] = $row;
    }
}

echo json_encode($readings);

$conn->close();
?>
