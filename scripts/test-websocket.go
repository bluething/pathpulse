package main

import (
	"encoding/json"
	"flag"
	"fmt"
	"log"
	"math/rand"
	"net/url"
	"os"
	"os/signal"
	"time"

	"github.com/gorilla/websocket"
)

type LocationData struct {
	UserID    string    `json:"userId"`
	Latitude  float64   `json:"latitude"`
	Longitude float64   `json:"longitude"`
	Accuracy  float64   `json:"accuracy"`
	Timestamp string    `json:"timestamp"`
}

func main() {
	addr := flag.String("addr", "localhost:8081", "http service address")
	path := flag.String("path", "/ws/location", "websocket path")
	count := flag.Int("count", 10, "number of updates to send")
	flag.Parse()

	log.SetFlags(0)

	interrupt := make(chan os.Signal, 1)
	signal.Notify(interrupt, os.Interrupt)

	u := url.URL{Scheme: "ws", Host: *addr, Path: *path}
	log.Printf("Connecting to %s", u.String())

	c, _, err := websocket.DefaultDialer.Dial(u.String(), nil)
	if err != nil {
		log.Fatal("dial:", err)
	}
	defer c.Close()

	done := make(chan struct{})

	go func() {
		defer close(done)
		for {
			_, message, err := c.ReadMessage()
			if err != nil {
				log.Println("read:", err)
				return
			}
			log.Printf("recv: %s", message)
		}
	}()

	ticker := time.NewTicker(500 * time.Millisecond)
	defer ticker.Stop()

	sentCount := 0
	for {
		select {
		case <-done:
			return
		case t := <-ticker.C:
			if sentCount >= *count {
				log.Printf("Sent %d updates. Closing connection.", *count)
				err := c.WriteMessage(websocket.CloseMessage, websocket.FormatCloseMessage(websocket.CloseNormalClosure, ""))
				if err != nil {
					log.Println("write close:", err)
					return
				}
				select {
				case <-done:
				case <-time.After(time.Second):
				}
				return
			}

			payload := LocationData{
				UserID:    fmt.Sprintf("user-%d", rand.Intn(5)+1),
				Latitude:  37.7749 + (rand.Float64()*1000)/100000.0,
				Longitude: -122.4194 + (rand.Float64()*1000)/100000.0,
				Accuracy:  rand.Float64()*50 + 1,
				Timestamp: t.UTC().Format("2006-01-02T15:04:05Z"),
			}

			data, err := json.Marshal(payload)
			if err != nil {
				log.Println("marshal:", err)
				continue
			}

			log.Printf("Sending: %s", string(data))
			err = c.WriteMessage(websocket.TextMessage, data)
			if err != nil {
				log.Println("write:", err)
				return
			}
			sentCount++

		case <-interrupt:
			log.Println("interrupt")

			// Cleanly close the connection by sending a close message and then
			// waiting (with timeout) for the server to close the connection.
			err := c.WriteMessage(websocket.CloseMessage, websocket.FormatCloseMessage(websocket.CloseNormalClosure, ""))
			if err != nil {
				log.Println("write close:", err)
				return
			}
			select {
			case <-done:
			case <-time.After(time.Second):
			}
			return
		}
	}
}
