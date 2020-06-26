
        <Container className="p-4 md-3">
        <Image src={URL_IMG} roundedCircle className="location p-2" />
        <Row>
          <Card className="mb-3">
            <Card.Body>
              <Card.Title>Allow Location Access?</Card.Title>
              <Card.Text>For best experience, Hyperlocal needs access to your location</Card.Text>
            </Card.Body>
          </Card>
        </Row>
        <Row>
          <Button variant="success" block className="pd-2"
            onClick={this.handleLocationInput}>Use My Location</Button>
        </Row>
        <Row>
          <InputGroup className="mt-3">
            <Form.Control
              placeholder="Or enter a location"
            />
            <Button variant="outline-primary">Go</Button>
          </InputGroup>
        </Row>
      </Container>