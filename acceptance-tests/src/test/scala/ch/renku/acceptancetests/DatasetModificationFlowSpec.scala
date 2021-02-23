/*
 * Copyright 2021 Swiss Data Science Center (SDSC)
 * A partnership between École Polytechnique Fédérale de Lausanne (EPFL) and
 * Eidgenössische Technische Hochschule Zürich (ETHZ).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.renku.acceptancetests

import ch.renku.acceptancetests.generators.Generators.Implicits._
import ch.renku.acceptancetests.generators.Generators.paragraph
import ch.renku.acceptancetests.model.datasets.{DatasetName, DatasetTitle}
import ch.renku.acceptancetests.model.projects.ProjectDetails
import ch.renku.acceptancetests.pages.ProjectPage
import ch.renku.acceptancetests.tooling.{AcceptanceSpec, DocsScreenshots, KnowledgeGraphApi}
import ch.renku.acceptancetests.workflows._

import scala.language.postfixOps

class DatasetModificationFlowSpec
    extends AcceptanceSpec
    with Login
    with NewProject
    with RemoveProject
    with Datasets
    with KnowledgeGraphApi {

  scenario("From the UI the user can modify a dataset and only interact with its latest version") {
    import Modification._
    implicit val loginType:       LoginType       = `log in to Renku`
    implicit val docsScreenshots: DocsScreenshots = new DocsScreenshots(this, browser)
    implicit val projectDetails:  ProjectDetails  = ProjectDetails.generate()
    implicit val projectPage:     ProjectPage     = ProjectPage()

    Given("a new renku project")
    createNewProject(projectDetails)

    And("a new dataset for this project")
    val datasetName         = DatasetName.generate
    val originalDatasetPage = `create a dataset`(datasetName)

    val newTitle       = DatasetTitle.generate
    val newDescription = paragraph().generateOne
    When("the user modifies the dataset")
    `modify the dataset`(originalDatasetPage,
                         by = `changing its title`(to = newTitle.toString),
                         and = `changing its description`(to = newDescription.value)
    )

    `remove project in GitLab`(projectDetails)
    `verify project is removed`

    `log out of Renku`
  }
}
