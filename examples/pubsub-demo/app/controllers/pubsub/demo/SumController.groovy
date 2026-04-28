package pubsub.demo

class SumController {

    SumService sumService
    TotalService totalService

    def index() {
        Random random = new Random()
        int sum = sumService.sum(random.nextInt(10), random.nextInt(20))
        int total = totalService.accumulatedTotal
        [sum: sum, total: total]
    }

}
