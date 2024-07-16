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

// Retrieve date parameter from GET request
$date = $_GET['date'];

// Prepare SQL query to fetch highest alcohol reading and time for the selected date
$sql = "SELECT MAX(V_AlcoholLevel) AS HighestAlcohol, V_Timestamp AS TimeOfHighest
        FROM tbl_values
        WHERE DATE(V_Timestamp) = '$date'";

$result = $conn->query($sql);

if ($result->num_rows > 0) {
    // Output data of each row
    $row = $result->fetch_assoc();
    $response = array(
        'HighestAlcohol' => $row['HighestAlcohol'],
        'TimeOfHighest' => $row['TimeOfHighest']
    );
    echo json_encode($response);
} else {
    // No results found for the selected date
    echo json_encode(array('HighestAlcohol' => 'N/A', 'TimeOfHighest' => 'N/A'));
}

$conn->close();
?>
