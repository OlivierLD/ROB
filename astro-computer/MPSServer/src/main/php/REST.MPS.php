<?php
/*
 * WiP, PoC.
 */

header("Content-Type: application/json");
// include 'db.php';

$method = $_SERVER['REQUEST_METHOD'];
$input = json_decode(file_get_contents('php://input'), true);

echo "Method is [" . $method . "]" . PHP_EOL;

// In the original doc, $pdo: PHP Data Object, see in https://medium.com/@dharshithasrimal/php-rest-api-7441197312d7
switch ($method) {
    case 'GET':
        handleGet();
        break;
    case 'POST':
        handlePost($input);
        break;
    case 'PUT':
        handlePut($input);
        break;
    case 'DELETE':
        handleDelete($input);
        break;
    default:
        echo json_encode(['message' => 'Invalid request method']);
        break;
}

function handleGet() {
    $sql = "SELECT * FROM users";
//    $stmt = $pdo->prepare($sql);
//    $stmt->execute();
//    $result = $stmt->fetchAll(PDO::FETCH_ASSOC);
    echo json_encode($sql);
}

function handlePost($input) {
    $sql = "INSERT INTO users (name, email) VALUES (:name, :email)";
//    $stmt = $pdo->prepare($sql);
//    $stmt->execute(['name' => $input['name'], 'email' => $input['email']]);
    echo json_encode(['message' => 'User created successfully', 'input' => $input]);
}

function handlePut($input) {
    $sql = "UPDATE users SET name = :name, email = :email WHERE id = :id";
//    $stmt = $pdo->prepare($sql);
//    $stmt->execute(['name' => $input['name'], 'email' => $input['email'], 'id' => $input['id']]);
    echo json_encode(['message' => 'User updated successfully']);
}

function handleDelete($input) {
    $sql = "DELETE FROM users WHERE id = :id";
//    $stmt = $pdo->prepare($sql);
//    $stmt->execute(['id' => $input['id']]);
    echo json_encode(['message' => 'User deleted successfully']);
}
?>