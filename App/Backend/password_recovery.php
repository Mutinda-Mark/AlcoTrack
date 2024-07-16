<?php
header('Content-Type: application/json');

$servername = "192.168.158.182";
$username = "root"; 
$password = ""; 
$dbname = "project-1"; 

$conn = new mysqli($servername, $username, $password, $dbname);

if ($conn->connect_error) {
    echo json_encode(["error" => "Connection failed: " . $conn->connect_error]);
    exit;
}

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $email = $_POST['email'];

    // Assuming D_ID is actually storing the email addresses.
    $sql = "SELECT D_Pass FROM tbl_driver WHERE D_ID = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("s", $email);
    $stmt->execute();
    $stmt->bind_result($d_pass);
    $stmt->fetch();

    if ($d_pass) {
        echo json_encode(["password" => $d_pass]);
    } else {
        echo json_encode(["error" => "No user found with the email: $email. Please check if it's registered."]);
    }

    $stmt->close();
}

$conn->close();
?>
